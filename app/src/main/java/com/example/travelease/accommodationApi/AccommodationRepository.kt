package com.example.travelease.accommodationsApi
import com.example.travelease.flightsApi.RetrofitClient

class AccommodationRepository {
    private val api = RetrofitClient.instance

    suspend fun fetchAccommodations(
        query: String,
        checkInDate: String,
        checkOutDate: String,
        sortBy: Int
    ): AccommodationResponse? {
        return try {
            val response = api.getAccommodations(
                query = query,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                sortBy = sortBy
            )

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