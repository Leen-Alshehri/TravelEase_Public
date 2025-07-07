package com.example.travelease.firebaseDB.entities

data class Activity(
    val name: String = "",
    val activityId:String="",
    val sdate:String="",
    val edate:String="",
    val location: String? = "N/A",
    val rating: Double = 0.00,
    val link:String="",
    val itineraryId: String = "",
    val description:String?="",
    val reviews:Int?=0
)

