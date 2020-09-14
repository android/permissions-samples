/*
* Copyright 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.basicpermissions

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.example.android.basicpermissions.camera.CameraPreviewActivity
import com.example.android.basicpermissions.util.checkSelfPermissionCompat
import com.example.android.basicpermissions.util.requestPermissionsCompat
import com.example.android.basicpermissions.util.shouldShowRequestPermissionRationaleCompat
import com.example.android.basicpermissions.util.showSnackbar

const val PERMISSION_REQUEST_CAMERA = 0

/**
 * Launcher Activity that demonstrates the use of runtime permissions for Android M.
 * This Activity requests permissions to access the camera
 * ([android.Manifest.permission.CAMERA])
 * when the 'Show Camera Preview' button is clicked to start  [CameraPreviewActivity] once
 * the permission has been granted.
 *
 * <p>First, the status of the Camera permission is checked using [ActivityCompat.checkSelfPermission]
 * If it has not been granted ([PackageManager.PERMISSION_GRANTED]), it is requested by
 * calling [ActivityCompat.requestPermissions]. The result of the request is
 * returned to the
 * [android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback], which starts
 * if the permission has been granted.
 *
 * <p>Note that there is no need to check the API level, the support library
 * already takes care of this. Similar helper methods for permissions are also available in
 * ([ActivityCompat],
 * [android.support.v4.content.ContextCompat] and [android.support.v4.app.Fragment]).
 */
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var layout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.main_layout)

        // Register a listener for the 'Show Camera Preview' button.
        findViewById<Button>(R.id.button_open_camera).setOnClickListener { showCameraPreview() }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                layout.showSnackbar(R.string.camera_permission_granted, Snackbar.LENGTH_SHORT)
                startCamera()
            } else {
                // Permission request was denied.
                layout.showSnackbar(R.string.camera_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }

    private fun showCameraPreview() {
        // Check if the Camera permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            layout.showSnackbar(R.string.camera_permission_available, Snackbar.LENGTH_SHORT)
            startCamera()
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission()
        }
    }

    /**
     * Requests the [android.Manifest.permission.CAMERA] permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private fun requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackbar(R.string.camera_access_required,
                    Snackbar.LENGTH_INDEFINITE, R.string.ok) {
                requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA),
                        PERMISSION_REQUEST_CAMERA)
            }

        } else {
            layout.showSnackbar(R.string.camera_permission_not_available, Snackbar.LENGTH_SHORT)

            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }

    private fun startCamera() {
        val intent = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intent)
    }
}
