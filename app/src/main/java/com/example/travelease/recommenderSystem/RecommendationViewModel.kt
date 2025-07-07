package com.example.travelease.recommenderSystem

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelease.firebaseDB.dbRepository
import com.example.travelease.flightsApi.BestFlight
import com.example.travelease.flightsApi.FlightRepository
import com.example.travelease.fromToMap
import kotlinx.coroutines.launch

class RecommendationViewModel : ViewModel() {

    private val repository = RecommendationRepository()

    private val flightRepository = FlightRepository()

    private val dbRepository = dbRepository()

    var destination = mutableStateOf<String?>("")

    var accommodation = mutableStateOf<accommodation?>(null)

    var activity = mutableStateOf<List<activity>>(emptyList())

    var flights = mutableStateOf<List<BestFlight>>(emptyList())
        private set

    var errorMessage = mutableStateOf("")
        private set

    var flightErrorMessage = mutableStateOf("")

    var isLoading = mutableStateOf(false)
        private set

    var sortBy = mutableStateOf(1)

    val startDate = mutableStateOf("")

    val endDate = mutableStateOf("")

    val tripName = mutableStateOf("")

    var imageUri = mutableStateOf<String?>(null)

    var addAllEnabled = mutableStateOf(false)


    fun getRecommendationWithFlight(
        date: String,
        travelerId: String
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val preferences = dbRepository.getPreferences(travelerId)
                val history = dbRepository.getHistory(travelerId)
                val userData = UserData(preferences, history)
                val recommendationReq = RecommendationRequest(startDate.value, endDate.value, userData)
                val response = repository.fetchRecommendation(recommendationReq)
                if (response != null) {
                    errorMessage.value = ""
                    destination.value = response.destination
                    accommodation.value = response.accommodation
                    if (response.activities != null)
                        activity.value = response.activities
                    val flightResponse = flightRepository.fetchFlights(
                        departureId = "RUH",
                        arrivalId = convertCityNameToAirportID(destination.value.toString()),
                        date = date,
                        times = "4,18",
                        sortBy = sortBy.value
                    )
                    val allFlights = mutableListOf<BestFlight>()

                    flightResponse?.best_flights?.let {
                        allFlights.addAll(it.map { flight ->
                            flight.copy(isDirect = flight.layovers.isNullOrEmpty())
                        })
                    }

                    flightResponse?.other_flights?.map { flight ->
                        BestFlight(
                            flight.flights,
                            flight.price,
                            flight.layovers,
                            flight.layovers.isNullOrEmpty()
                        )
                    }?.let { allFlights.addAll(it) }
                    if (allFlights.isNotEmpty()) {
                        flights.value = allFlights
                    } else
                        flightErrorMessage.value = "No flights found"
                } else {
                    errorMessage.value = "No recommendation found"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun convertCityNameToAirportID(city: String): String {
        return fromToMap[city].toString()

    }

    fun setStartDate(date: String) {
        startDate.value = date
    }

    fun setEndDate(date: String) {
        endDate.value = date
    }

    fun setTripName(name: String) {
        tripName.value = name
    }

    fun setImageUri(image: String?) {
        imageUri.value = image
    }

    fun setAddAllButton(flag: Boolean) {
        addAllEnabled.value = flag
    }
}