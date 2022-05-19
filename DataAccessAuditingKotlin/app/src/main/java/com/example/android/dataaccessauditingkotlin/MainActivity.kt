/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.dataaccessauditingkotlin

import android.Manifest
import android.app.AppOpsManager
import android.app.AsyncNotedAppOp
import android.app.SyncNotedAppOp
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AppOpsManagerCompat.noteOp
import androidx.databinding.DataBindingUtil
import com.example.android.dataaccessauditingkotlin.databinding.ActivityMainBinding
import com.examplelibrary.android.shoppinglibrary.ShoppingLibrary
import com.google.android.material.snackbar.Snackbar

private const val TAG = "MainActivity"
private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34

/**
 * Audits/Logs protected data via the {@link android.app.AppOpsManager.OnOpNotedCallback} APIs.
 *
 * Protected data is data that's protected by runtime or app op permissions. Your app will not have
 * access to this data until the user has granted your app access. Examples include location,
 * contacts, etc.
 *
 * <p>
 * The {@link android.app.AppOpsManager.OnOpNotedCallback} provides three callbacks:
 * <ul>
 * <li>onNoted - Called when protected data is accessed via a synchronous call. For example, onNoted
 * would be triggered if an app requested the user's last known location and that function returns
 * the value synchronously (right away).
 *
 * <li>onAsyncNoted - Called when protected data is accessed via an asynchronous callback. For
 * example, if an app subscribed to location changes, onAsyncNoted would be triggered when the
 * callback is invoked and returns a new location. A Geofence is another example.
 *
 * <li>onSelfNoted - Called when a developer calls {@link android.app.AppOpsManager#noteOp} or
 * {@link android.app.AppOpsManager#noteProxyOp} to manually report access to protected data from
 * it's own {@link android.os.Processl#myUid}. This is the only callback that isn't triggered by
 * the system. It's a way for apps to to blame themselves when they feel like they are accessing
 * protected data and want to audit it.
 *
 * If your app is the initial data provider, you want to use
 * {@link android.app.AppOpsManager#noteOp}. For our sample, we will use
 * {@link android.app.AppOpsManager#noteOp}.
 *
 * It's a fairly uncommon use case, so in most cases, you won't need to do this.
 * </ul>
 * <p>
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val opsNotedForThisApp: MutableList<Pair<String, String>> = mutableListOf()

    // Most external libraries (local or third party) require a context. When passing that context,
    // you should set a attribution tag/context as done below. This makes it easier to determine
    // which part of your app is accessing protected data (done in this sample via the
    // OnOpNotedCallback).
    private val shoppingLibrary by lazy {
        val specializedContextWithAttributionTag =
            applicationContext.createAttributionContext("Shopping 3rd party library")
        ShoppingLibrary(specializedContextWithAttributionTag)
    }

    private val onOpNotedCallback = object : AppOpsManager.OnOpNotedCallback() {

        // Saves and logs information.
        // Potentially called from different threads, so synchronized. Could be further optimized to
        // just the portion dealing with the MutableList.
        @Synchronized
        private fun saveAndLog(operation: String, stackTrace: String) {
            // Save
            opsNotedForThisApp.add(Pair(operation, stackTrace))

            // Log
            Log.d(TAG, operation)
            Log.d(TAG, stackTrace)

            // Output to screen
            val operationsOnly =
                opsNotedForThisApp.map { "- ${it.first}" }

            runOnUiThread {
                binding.outputTextView.text =
                    operationsOnly.joinToString("\n", "Check Log for stacktrace.\n", "\n")
            }
        }

        /**
         * onNoted - Called when protected data is accessed via a synchronous call. For example,
         * onNoted would be triggered if an app requested the user's last known location and that
         * function returns the value synchronous (right away).
         */
        override fun onNoted(operation: SyncNotedAppOp) {
            val operationDescription =
                prettyOperationDescription("onNoted()", operation.op, operation.attributionTag)

            val prettyStackTrace = prettyStackTrack(Thread.currentThread().stackTrace)

            saveAndLog(operationDescription, prettyStackTrace)
        }

        /**
         * onAsyncNoted - Called when protected data is accessed via an asynchronous callback. For
         * example, if an app subscribed to location changes, onAsyncNoted would be triggered when
         * the callback with a new location is called. A Geofence is another example.
         *
         * IMPORTANT NOTE: Because you are waiting on a GPS signal update, this might take a minute
         * or two to show up after you click the button.
         */
        override fun onAsyncNoted(asyncOp: AsyncNotedAppOp) {
            val operationDescription =
                prettyOperationDescription("onAsyncNoted()", asyncOp.op, asyncOp.attributionTag)

            // For an AsyncNotedAppOp, it's more effective to use the 'message' field instead of
            // retrieving the stack trace of the current thread to identify the call.
            val message = asyncOp.message

            saveAndLog(operationDescription, message)
        }

        /**
         *  onSelfNoted - Called when a developer calls {@link android.app.AppOpsManager#noteOp}
         *  to manually trigger a protected data access. This is the only callback that isn't
         *  triggered by the system. It's a way for apps to to blame themselves when they feel like
         *  they are accessing protected data and want to audit it.
         *
         *  It's a fairly uncommon use case, so in most cases, you won't need to do this.
         */
        override fun onSelfNoted(operation: SyncNotedAppOp) {
            val operationDescription =
                prettyOperationDescription("onSelfNoted()", operation.op, operation.attributionTag)

            val prettyStackTrace = prettyStackTrack(Thread.currentThread().stackTrace)

            saveAndLog(operationDescription, prettyStackTrace)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Assigns noted operations callback to receive updates on operations.
        // Note: If you want to provide constant, global tracking, you should call
        // {@link android.app.AppOpsManager#setOnOpNotedCallback} at the Application level.
        val applicationOperationManager = getSystemService(AppOpsManager::class.java)
        applicationOperationManager?.setOnOpNotedCallback(mainExecutor, onOpNotedCallback)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onStop() {
        super.onStop()
        shoppingLibrary.finishedWithLibrary()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if ((requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) &&
            (grantResults[0] == PackageManager.PERMISSION_DENIED)) {
            Toast.makeText(
                this,
                getString(R.string.location_permission_required_toast_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateUI() {
        val permissionApproved =
            applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            if (opsNotedForThisApp.isEmpty()) {
                binding.outputTextView.text = getString(R.string.empty_list_screen_text)
            }
            binding.locationPermissionRequestButton.visibility = View.GONE

            binding.localSynchronousFunctionButton.visibility = View.VISIBLE
            binding.librarySynchronousFunctionButton.visibility = View.VISIBLE
            binding.libraryAsynchronousFunctionButton.visibility = View.VISIBLE
            binding.manuallyNoteOperationButton.visibility = View.VISIBLE
        } else {
            binding.locationPermissionRequestButton.visibility = View.VISIBLE

            binding.localSynchronousFunctionButton.visibility = View.GONE
            binding.librarySynchronousFunctionButton.visibility = View.GONE
            binding.libraryAsynchronousFunctionButton.visibility = View.GONE
            binding.manuallyNoteOperationButton.visibility = View.GONE
        }
    }

    // All button click methods:
    fun onClickLocationPermissionRequest(view: View) {
        val permissionApproved =
            applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (!permissionApproved) {
            // Build SnackBar in case the permission needs extra rationale.
            val snackBar = Snackbar.make(
                binding.container,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.ok)) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                    )
                }

            requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                snackBar
            )
        }
    }

    /**
     * Accesses protected data in a local function.
     *
     * This type of access will trigger onNoted().
     */
    fun onClickLocalSynchronousFunction(view: View) {
        Log.d(TAG, "onClickLocalFunction()")

        val wifiManager = applicationContext.getSystemService(WifiManager::class.java)
        // Protected data call; app would do something with this data.
        val scanResults = wifiManager.scanResults
    }

    /**
     * Calls a library function that also accesses protected data immediately. An example would be
     * the library retrieving the last known location which returns the result immediately, that
     * is, there is no callback to wait for the result.
     *
     * This type of access will trigger onNoted().
     */
    fun onClickLibrarySynchronousFunction(view: View) {
        Log.d(TAG, "onClickExternalLibraryGetFavoriteShop()")
        val shop = shoppingLibrary.getFavoriteLocalShop()
        Log.d(TAG, "Retrieved favorite shop: $shop")
    }

    /**
     * Calls a library function that also accesses protected data through a callback. An example
     * would be the library subscribing to location changes and receiving the data via a callback
     * when a new location is provided.
     *
     * This type of access will trigger onAsyncNoted(), since the protected data is accessed later
     * in a callback.
     *
     * IMPORTANT NOTE: Because you are waiting on a GPS signal update, the callback might take a
     * minute or two to show up after you click the button.
     */
    fun onClickLibraryAsynchronousFunction(view: View) {
        Log.d(TAG, "onClickExternalLibraryGetLocalShops()")
        val localShops = shoppingLibrary.getListOfLocalShops()
        Log.d(TAG, "Local Shops: $localShops")
    }

    /**
     * AppOpsManager.noteOp() allows you to manually note an operation.
     *
     * This is a rare use case. For most data access auditing, you will simply assign an
     * OnOpNotedCallback and listen for protected data usage (tracked by the system).
     */
    fun onClickManuallyNoteOperation(view: View) {

        /**
         * Make note of an application performing an operation. Note that you must pass in both the
         * uid and name of the application to be checked; this function will verify that these two
         * match, and if not, return MODE_IGNORED.
         *
         * If this call succeeds, the last execution time of the operation for this app will be
         * updated to the current time.
         *
         * If this is a check that is not preceding the protected operation, use
         * unsafeCheckOp(String, int, String) instead.
         *
         * For more information, see {@link android.app.AppOpsManager#noteOp}.
         */
        noteOp(applicationContext, AppOpsManager.OPSTR_FINE_LOCATION, Process.myUid(), packageName)

        // Protected operation called after noteOp.
        applicationContext.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun prettyOperationDescription(
        methodName: String,
        operation: String,
        attributionTag: String?
    ) = "$methodName: $operation, Attribution Tag: ${attributionTag ?: "NONE"}"

    private fun prettyStackTrack(stackTraceElements: Array<StackTraceElement>): String {
        return stackTraceElements.joinToString("\n", "Stack Trace:\n", "\n") { "\t$it" }
    }
}
