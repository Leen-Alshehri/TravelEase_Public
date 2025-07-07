package com.example.travelease.weatherNotifications

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherService = retrofit.create(WeatherService::class.java)

    fun fetchWeather(context: Context, apiKey: String, onResult: (WeatherResponse?) -> Unit) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                fusedClient.removeLocationUpdates(this)

                if (location != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = weatherService.getCurrentWeather(
                                lat = location.latitude,
                                lon = location.longitude,
                                apiKey = apiKey
                            )
                            onResult(response)
                        } catch (e: Exception) {
                            Log.e("WeatherViewModel", "Error: ${e.message}")
                            onResult(null)
                        }
                    }
                } else {
                    Log.e("WeatherViewModel", "Location is null")
                    onResult(null)
                }
            }
        }

        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            Log.e("WeatherViewModel", "Permission not granted")
            onResult(null)
        }
    }
}

