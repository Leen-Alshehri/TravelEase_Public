package com.example.travelease.firebaseDB.entities

data class Trip(
    val tripId: String = "",
    val travelerId: String = "",
    val itineraryId:String="",
    val name: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val imageUrl: String? = null // Optional
)
