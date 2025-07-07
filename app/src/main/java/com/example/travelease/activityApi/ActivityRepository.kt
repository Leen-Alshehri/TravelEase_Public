package com.example.travelease.activityApi

import com.example.travelease.flightsApi.RetrofitClient

class ActivityRepository {
    private val api = RetrofitClient.instance

    suspend fun fetchActivities(query: String): ActivitiesResponse? {
        return try {
            println("Fetching activities with query: $query")
            val response = api.getActivities(query = query)
            println("Response received: $response")
            response
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("HTTP Error: ${e.code()} - $errorBody")
            null
        } catch (e: Exception) {
            println("Network Error: ${e.message}")
            null
        }
    }
}