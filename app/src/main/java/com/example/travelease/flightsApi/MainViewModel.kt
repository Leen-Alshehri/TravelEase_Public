package com.example.travelease.flightsApi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import com.example.travelease.fromToMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    var fromText = mutableStateOf("Departure")
    var toText = mutableStateOf("Destination")
    var flightStartDate = mutableStateOf("")
    var expanded = mutableStateOf(false)
    var expanded2 = mutableStateOf(false)

    private val repository = FlightRepository()

    var flights = mutableStateOf<List<BestFlight>>(emptyList())
        private set

    var errorMessage = mutableStateOf("")
        private set

    var sortBy = mutableStateOf(1)// stores sorting optiin picked by user

    fun getFlights() {//gets flights from repository (first thing to get triggered when the user clicks serach)
        viewModelScope.launch {
            try {
                val response = repository.fetchFlights(//calling the repository function that fetches the flight data
                    departureId = convertCityNameToAirportID(fromText.value) ,// converting the city name to airport id
                    arrivalId = convertCityNameToAirportID(toText.value),
                    date = convertLongToTime(convertDateToLong(flightStartDate.value)),// formatting the selected date
                    times = "1,23",// between 1 am and 23 (11) pm
                    sortBy = sortBy.value
                )
                val allFlights = mutableListOf<BestFlight>()//empty list where we put all the flights
                response?.best_flights?.let {//if theres best flights:
                    allFlights.addAll(it.map { flight ->//copy and add the flights to the list
                        flight.copy(isDirect = flight.layovers.isNullOrEmpty())// if there are no layovers, the flight is direct
                    })
                }
                response?.other_flights?.map { flight ->
                    BestFlight(flight.flights, flight.price, flight.layovers, flight.layovers.isNullOrEmpty())
                }?.let { allFlights.addAll(it) }// same process for other flights
                if (allFlights.isNotEmpty()) {// now check if there exists at least 1 flight
                    flights.value = allFlights// updates flight with all flights
                } else {
                    errorMessage.value = "No flights found"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun updateSortBy(newSort: Int) {
        sortBy.value = newSort
        getFlights()
    }

    fun convertCityNameToAirportID(city: String): String {
        return fromToMap[city].toString()

    }

    fun setStartDateForFlight(date: String) {
        flightStartDate.value = date
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}
