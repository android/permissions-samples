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
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android.basicpermissions.camera.CameraPreviewActivity
import com.example.android.basicpermissions.databinding.ActivityMainBinding
import com.example.android.basicpermissions.util.showSnackbar
import com.google.android.material.snackbar.Snackbar

/**
 * Launcher Activity that demonstrates the use of runtime permissions for Android.
 * This Activity requests permissions to access the camera
 * ([android.Manifest.permission.CAMERA])
 * when the 'Show Camera Preview' button is clicked to start  [CameraPreviewActivity] once
 * the permission has been granted.
 *
 * <p>First, the status of the Camera permission is checked using
 * [ContextCompat.checkSelfPermission].
 * If it has not been granted ([PackageManager.PERMISSION_GRANTED]), it is requested by
 * calling [ActivityResultContracts.RequestPermission]. The result of the request is
 * returned to the
 * [androidx.activity.result.ActivityResultCaller.registerForActivityResult], which starts
 * if the permission has been granted.
 *
 */
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var layout: View

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    private val requestPermissionLauncher =
            registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission has been granted. Start camera preview Activity.
                    layout.showSnackbar(
                            R.string.camera_permission_granted,
                            Snackbar.LENGTH_INDEFINITE,
                            R.string.ok
                    ) {
                        startCamera()
                    }
                } else {
                    // Permission request was denied.
                    layout.showSnackbar(
                            R.string.camera_permission_denied,
                            Snackbar.LENGTH_SHORT,
                            R.string.ok)
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        layout = binding.root

        setContentView(layout)

        // Register a listener for the 'Show Camera Preview' button.
        binding.buttonOpenCamera.setOnClickListener { showCameraPreview() }
    }

    private fun showCameraPreview() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            layout.showSnackbar(
                    R.string.camera_permission_available,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.ok
            ) {
                startCamera()
            }
        } else requestCameraPermission()
    }

    /**
     * Requests the [android.Manifest.permission.CAMERA] permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            layout.showSnackbar(
                    R.string.camera_access_required,
                    Snackbar.LENGTH_INDEFINITE,
                    R.string.ok
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            // You can directly ask for the permission.
            layout.showSnackbar(
                    R.string.camera_permission_not_available,
                    Snackbar.LENGTH_LONG,
                    R.string.ok
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val intent = Intent(this, CameraPreviewActivity::class.java)
        startActivity(intent)
    }
}
