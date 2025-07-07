package com.example.travelease


val fromToList: List<String> = listOf(
    "Riyadh", "Amman", "Amsterdam", "Atlanta", "Athens", "Bali", "Bangkok", "Barcelona",
    "Beijing", "Beirut", "Berlin", "Boston", "Brussels", "Cairo", "Cape Town", "Chicago", "Dallas", "Dammam",
    "Delhi", "Doha", "Dubai", "Dublin", "Frankfurt", "Hanoi", "Houston", "Istanbul", "Jeddah",
    "Kuwait City", "Lisbon", "London", "Los Angeles", "Madrid", "Manama", "Medina", "Miami", "Moscow",
    "Muscat", "New York", "Paris", "Abu Dhabi", "Rome", "San Francisco", "Seoul", "Singapore",
    "Stockholm", "Sydney", "Tokyo", "Toronto", "Vancouver", "Vienna", "Washington, D.C.", "Zurich"
)

val fromToMap = mapOf(
    "Abu Dhabi" to "AUH",
    "Amman" to "AMM",
    "Amsterdam" to "AMS",
    "Atlanta" to "ATL",
    "Athens" to "ATH",
    "Bali" to "DPS",
    "Bangkok" to "BKK",
    "Barcelona" to "BCN",
    "Beijing" to "PEK",
    "Beirut" to "BEY",
    "Berlin" to "BER",
    "Boston" to "BOS",
    "Brussels" to "BRU",
    "Cairo" to "CAI",
    "Cape Town" to "CPT",
    "Chicago" to "ORD",
    "Dallas" to "DFW",
    "Dammam" to "DMM",
    "Delhi" to "DEL",
    "Doha" to "DOH",
    "Dubai" to "DXB",
    "Dublin" to "DUB",
    "Frankfurt" to "FRA",
    "Hanoi" to "HAN",
    "Houston" to "IAH",
    "Istanbul" to "IST",
    "Jeddah" to "JED",
    "Kuwait City" to "KWI",
    "Lisbon" to "LIS",
    "London" to "LHR",
    "Los Angeles" to "LAX",
    "Madrid" to "MAD",
    "Manama" to "BAH",
    "Medina" to "MED",
    "Miami" to "MIA",
    "Moscow" to "SVO",
    "Muscat" to "MCT",
    "New York" to "JFK",
    "Paris" to "CDG",
    "Riyadh" to "RUH",
    "Rome" to "FCO",
    "San Francisco" to "SFO",
    "Seoul" to "ICN",
    "Singapore" to "SIN",
    "Stockholm" to "ARN",
    "Sydney" to "SYD",
    "Tokyo" to "NRT",
    "Toronto" to "YYZ",
    "Vancouver" to "YVR",
    "Vienna" to "VIE",
    "Washington, D.C." to "IAD",
    "Zurich" to "ZRH"
)


fun getMonthName(month: Int): String = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month]