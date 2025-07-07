package com.example.travelease.firebaseDB.entities

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Itinerary(
    val itineraryId: String = "",
    val tripId: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) // Use today’s date if empty
)
