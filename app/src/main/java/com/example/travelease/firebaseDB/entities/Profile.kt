package com.example.travelease.firebaseDB.entities

data class Profile(
    val profileId: String = "",
    val travelerId: String = "",
    val preferenceList: MutableMap<String, String> = mutableMapOf(), //answers of the preference selection questions after sign up
    val history: MutableMap<String, String> = mutableMapOf() //hold traveler's trip history
)

