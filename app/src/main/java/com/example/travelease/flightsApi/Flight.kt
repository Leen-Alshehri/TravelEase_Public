package com.example.travelease.flightsApi
//flight data class that acts like a container for the data
data class Airport(
    val name: String,
    val id: String,
    val time: String
) {
    constructor() : this("", "", "")
}

data class Layover(
    val duration: Int,
    val name: String,
    val id: String,
    val overnight: Boolean
) {
    constructor() : this(0, "", "", false)
}

data class Flight(
    val departure_airport: Airport,
    val arrival_airport: Airport,
    val duration: Int,
    val airline: String,
    val flight_number: String,
    val date: String,

    val airplane: String? = null,
    val airline_logo: String? = null,
    val travel_class: String? = null,
    val extensions: List<String>? = null,
    val ticket_also_sold_by: List<String>? = null,
    val legroom: String? = null,
    val overnight: Boolean? = null,
    val often_delayed_by_over_30_min: Boolean? = null,
    val plane_and_crew_by: String? = null
)

data class CarbonEmissions(
    val this_flight: Int?,
    val typical_for_this_route: Int?,
    val difference_percent: Int?
)

data class BestFlight(
    val flights: List<Flight>,
    val price: Int,
    val layovers: List<Layover>?,
    val isDirect: Boolean,

    val total_duration: Int? = null,
    val carbon_emissions: CarbonEmissions? = null,
    val type: String? = null,
    val airline_logo: String? = null,
    val extensions: List<String>? = null,
    val departure_token: String? = null,
    val booking_token: String? = null
)

data class OtherFlight(
    val flights: List<Flight>,
    val price: Int,
    val layovers: List<Layover>?,
    val isDirect: Boolean,
    val total_duration: Int? = null,
    val carbon_emissions: CarbonEmissions? = null,
    val type: String? = null,
    val airline_logo: String? = null,
    val extensions: List<String>? = null,
    val departure_token: String? = null,
    val booking_token: String? = null
)

data class FlightResponse(
    val best_flights: List<BestFlight>?,
    val other_flights: List<OtherFlight>?
)
