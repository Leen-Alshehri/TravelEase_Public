package com.example.travelease.weatherNotifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travelease.R
import com.example.travelease.weatherNotifications.WeatherWorker.*
import java.util.concurrent.TimeUnit

class WeatherRepository(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchWeatherAndNotify(): WeatherResponse? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return null //skips fetching if location permission is not granted.

        val response = WeatherApi.getWeatherByLocation(context) //get the current weather using GPS
        val condition = response?.weather?.firstOrNull()?.main ?: return null //Extracts the main weather condition
        val message = getWeatherMessage(condition)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendWeatherNotification(message) //Displays the message via notification
        }

        return response
    }

    private fun getWeatherMessage(condition: String): String { //this maps weather condition to message
        return when (condition.lowercase()) {
            "rain", "drizzle", "thunderstorm", "fog", "haze", "mist", "dust", "smoke" ->
                "Weather Alert: You should consider the weather for your activities."
            "clear", "clouds" ->
                "Great weather! Enjoy your trip and outdoor activities."
            else ->
                "Stay safe and check the latest weather before heading out."
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
     fun sendWeatherNotification(message: String) {
        val channelId = "weather_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel) //Creates a channel, and a high-priority notification

        val notification = NotificationCompat.Builder(context, channelId) //build popup message
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Weather Update")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        manager.notify(102, notification) //send it to the system tray
    }

    fun startWeatherNotifications() { //This runs once at first app launches, then every 15 minutes
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val hasLaunchedOnce = prefs.getBoolean("started_once", false)

        if (!hasLaunchedOnce) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<WeatherWorker>().build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            prefs.edit().putBoolean("started_once", true).apply()
        }

        val periodicRequest = PeriodicWorkRequestBuilder<WeatherWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeatherNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}

