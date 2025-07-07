package com.example.travelease.flightsApi

class FlightRepository {
    private val api = RetrofitClient.instance// instance of the api

    suspend fun fetchFlights(
        departureId: String,
        arrivalId: String,
        date: String,
        times: String,
        sortBy: Int
    ): FlightResponse? {
        return try {
            val response = api.getFlights(
                departureId = departureId,
                arrivalId = arrivalId,
                outboundDate = date,
                outboundTimes = times,
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

