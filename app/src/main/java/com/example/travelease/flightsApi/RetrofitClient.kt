package com.example.travelease.flightsApi

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://serpapi.com/"// base url for the api

    private val client: OkHttpClient = OkHttpClient.Builder()//client for the api OkHttp
        .build()

    val instance: SerpApiService by lazy {//creates a Retrofit instance only when it’s first needed (once only)
        Retrofit.Builder()// create retrofit instance
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SerpApiService::class.java)
    }
}











//JSON response from the API is automatically converted to FlightResponse data class using GsonConverterFactory
//USER ⟶ FlightScreen UI  ───────┐
//                               ▼
//                      MainViewModel.getFlights()
//                               ▼
//                FlightRepository.fetchFlights()
//                               ▼
//           RetrofitClient.instance.getFlights(...) ──→  [API: serpapi.com/search.json]
//                                                       ↳ Returns JSON
//                               ▼
//                    Gson parses to FlightResponse
//                               ▼
//     ViewModel processes data into List<BestFlight>
//                               ▼
//   mutableStateOf(flights) updated, Compose recomposes
//                               ▼
// UI shows: FlightCard / FlightCardWithTripPicker (based on mode)