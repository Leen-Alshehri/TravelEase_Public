package com.example.travelease.firebaseDB.entities

data class Accommodation(
    val name: String = "",
    val accommodationId:String="",
    val itineraryId: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val location: String? = null,
    val description:String?=null,
    val hotelClass:String?=null,
    val pricePerNight: String? = "",
    val rating: Float = 0f,
    val link: String = "",
    val price:Int=0,
    val reviews:Int?=0
)
