package com.example.travelease.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

object LocationUtils {
    fun getLastKnownLocation(context: Context, onLocationReceived: (Location?) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            onLocationReceived(null)
            return
        }
        client.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationReceived(location)
        }
    }
}
