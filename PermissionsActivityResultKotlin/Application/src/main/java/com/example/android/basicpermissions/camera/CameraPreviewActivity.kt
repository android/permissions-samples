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

package com.example.android.basicpermissions.camera

import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.basicpermissions.databinding.ActivityCameraBinding
import com.example.android.basicpermissions.databinding.ActivityCameraUnavailableBinding

private const val TAG = "CameraPreviewActivity"
private const val CAMERA_ID = 0

/**
 * Displays a [CameraPreview] of the first [Camera].
 * An error message is displayed if the Camera is not available.
 *
 *
 * This Activity is only used to illustrate that access to the Camera API has been granted (or
 * denied) as part of the runtime permissions model. It is not relevant for the use of the
 * permissions API.
 *
 *
 * Implementation is based directly on the documentation at
 * http://developer.android.com/guide/topics/media/camera.html
 */
class CameraPreviewActivity : AppCompatActivity() {

    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Open an instance of the first camera and retrieve its info.
        camera = getCameraInstance(CAMERA_ID)
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(CAMERA_ID, cameraInfo)

        if (camera == null) {
            // Camera is not available, display error message.
            val binding = ActivityCameraUnavailableBinding.inflate(layoutInflater)
            setContentView(binding.root)
        } else {
            val binding = ActivityCameraBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Get the rotation of the screen to adjust the preview image accordingly.
            val displayRotation = windowManager.defaultDisplay.rotation

            // Create the Preview view and set it as the content of this Activity.
            val cameraPreview = CameraPreview(this, null, 0, camera, cameraInfo, displayRotation)
            binding.cameraPreview.addView(cameraPreview)
        }
    }

    public override fun onPause() {
        super.onPause()
        // Stop camera access
        releaseCamera()
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    private fun getCameraInstance(cameraId: Int): Camera? {
        var cameraInstance: Camera? = null
        try {
            cameraInstance = Camera.open(cameraId) // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera $cameraId is not available: ${e.message}")
        }

        return cameraInstance // returns null if camera is unavailable
    }

    private fun releaseCamera() {
        camera?.release()
        camera = null
    }
}
