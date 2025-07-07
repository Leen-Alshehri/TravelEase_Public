package com.example.travelease.weatherNotifications

import retrofit2.http.GET
import retrofit2.http.Query



interface WeatherService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

data class WeatherResponse(
    val weather: List<WeatherCondition>,
    val main: MainInfo,
    val name: String
)

data class WeatherCondition(
    val main: String,
    val description: String,
    val icon: String
)

data class MainInfo(
    val temp: Float
)
