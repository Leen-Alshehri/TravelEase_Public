package com.example.travelease.flightsApi

import com.example.travelease.activityApi.ActivitiesResponse
import com.example.travelease.trendingPlacesApi.HighResImageResponse
import com.example.travelease.trendingPlacesApi.TrendingResponse
import com.example.travelease.accommodationsApi.AccommodationResponse
import retrofit2.http.GET
import retrofit2.http.Query

// api endpoints and their parameters to minimize results
interface SerpApiService {
    @GET("search")
    suspend fun getFlights(
        @Query("engine") engine: String = "google_flights",
        @Query("departure_id") departureId: String,
        @Query("arrival_id") arrivalId: String,
        @Query("currency") currency: String = "SAR",
        @Query("outbound_date") outboundDate: String,
        @Query("outbound_times") outboundTimes: String,
        @Query("sort_by") sortBy: Int,
        @Query("type") type: Int = 2,
        @Query("hl") language: String = "en",
        @Query("api_key") apiKey: String = "REMOVED"
    ): FlightResponse

    @GET("search")
    suspend fun getAccommodations(
        @Query("engine") engine: String = "google_hotels",
        @Query("q") query: String,
        @Query("check_in_date") checkInDate: String,
        @Query("check_out_date") checkOutDate: String,
        @Query("currency") currency: String = "SAR",
        @Query("hotel_class") hotelClass: Int = 5,
        @Query("sort_by") sortBy: Int,
        @Query("hl") language: String = "en",
        @Query("api_key") apiKey: String = "REMOVED"
    ): AccommodationResponse
    @GET("search")
    suspend fun getActivities(
        @Query("engine") engine: String = "google_events",
        @Query("q") query: String,
        @Query("api_key") apiKey: String = "v"
    ): ActivitiesResponse
    @GET("search")
    suspend fun getTrendingDestinations(
        @Query("engine") engine: String = "google",
        @Query("q") query: String,
       // @Query("tbs") tbs: String = "isz:lt,islt:4mp",
        @Query("api_key") apiKey: String = "REMOVED"
    ): TrendingResponse
    @GET("search")
    suspend fun getHighResImage(
        @Query("engine") engine: String = "google",
        @Query("q") query: String,
        @Query("tbm") tbm: String = "isch",
        @Query("api_key") apiKey: String = "REMOVED"
    ): HighResImageResponse
}