package com.example.travelease.weatherNotifications

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// This class represent the Weather API response for notifications
@RequiresApi(Build.VERSION_CODES.O)
class WeatherWorker( //This is background task, runs even when the app is in background
    val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result { //Main entry point.
        //Fetches and shows weather
        return try {
            val repository = WeatherRepository(context)
            repository.fetchWeatherAndNotify()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }





    object WeatherApi {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
        private const val API_KEY = "REMOVED"

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val service: WeatherService = retrofit.create(WeatherService::class.java)

        suspend fun getWeatherByLocation(context: Context): WeatherResponse? {
            return try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return null
                }

                val location: Location? = fusedLocationClient.lastLocation.await() //Gets user's last known location
                location?.let {
                    service.getCurrentWeather(it.latitude, it.longitude, API_KEY) //Sends user's coordinates to OpenWeatherMap and returns the weather data
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}


