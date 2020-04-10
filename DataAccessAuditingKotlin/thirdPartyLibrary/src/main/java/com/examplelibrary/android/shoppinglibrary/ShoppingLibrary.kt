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
package com.examplelibrary.android.shoppinglibrary

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Looper
import com.example.android.internallibrary.hasPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

/**
 * Simple library that provides a favorite local shop and a list of local shops. It uses location
 * sometimes with the method calls.
 */
class ShoppingLibrary(private val context: Context) {

    private var receivingLocationUpdates = false

    private val wifiManager = context.getSystemService(WifiManager::class.java)

    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Stores parameters for requests to the FusedLocationProviderApi.
    private val locationRequest = LocationRequest().apply {
        // Sets the desired interval for active location updates. This interval is inexact. You
        // may not receive updates at all if no location sources are available, or you may
        // receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        //
        // IMPORTANT NOTE: Apps running on "O" devices (regardless of targetSdkVersion) may
        // receive updates less frequently than this interval when the app is no longer in the
        // foreground.
        interval = TimeUnit.SECONDS.toMillis(10)

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        fastestInterval = TimeUnit.SECONDS.toMillis(5)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        maxWaitTime = TimeUnit.MINUTES.toMillis(1)

        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            // Does something with location update shops local database (if it existed).
        }
    }

    fun getFavoriteLocalShop(): String {
        val scanResults = wifiManager.scanResults
        return "Cool Coffee Shop, Inc."
    }

    fun getListOfLocalShops(): List<String> {
        startLocationUpdates()
        return getLocalShops()
    }

    private fun startLocationUpdates() {
        val permissionApproved =
            context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved and !receivingLocationUpdates) {
            receivingLocationUpdates = true

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }
    }

    private fun getLocalShops(): List<String> =
        listOf("Coffee Shop 1, Inc.", "Tea Shop 2, Inc.", "Decaf Shop 3, Inc.")

    fun finishedWithLibrary() {
        if (receivingLocationUpdates) {
            receivingLocationUpdates = false
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
