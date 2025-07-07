package com.example.travelease.firebaseDB.entities


data class Traveler(
    val travelerId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val dateOfBirth: String = "" ,// Stored as String in Firestore
    val userimage:String ?=null
    )

