package com.example.travelease.firebaseDB.entities

import com.example.travelease.flightsApi.Airport
import com.example.travelease.flightsApi.Layover

data class Flight(
    val flightId:String="",
    val flightNo: String = "",
    val itineraryId: String = "",
    val from: String = "",
    val to: String = "",
    val price: Double=0.00,
    val direct:String="",
    val airline:String="",
    val departureTime:String="",
    val arrivalTime:String="",
    val duration:Int=0,
    val layovers: List<Layover>?=null,
    val segments: List<FlightSegment>? = null,
    val flightDate: String = ""
    ){
    // Firestore requires an empty constructor
    constructor() : this("", "", "", "", "", 0.00, "", "", "", "", 0,null,null, "")
}
data class FlightSegment(
    val departure_airport: Airport = Airport(),
    val arrival_airport: Airport = Airport(),
    val duration: Int = 0,
    val airline: String = "",
    val flight_number: String = ""
) {
    constructor() : this(Airport(), Airport(), 0, "", "")
}

