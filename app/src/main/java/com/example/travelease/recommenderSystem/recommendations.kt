package com.example.travelease.recommenderSystem

import com.google.gson.annotations.SerializedName


data class Recommendation(
    val destination: String?,
    val accommodation: accommodation?,
    @SerializedName("activitie") val activities: List<activity>?

)

data class accommodation(
    val name: String,
    val description: String,
    val rating: Float,
    val reviews: Int,
    val price: Float,
    val link: String,
    val image: String,
    @SerializedName("check_in") val checkin: String,
    @SerializedName("check_out") val checkout: String
)

data class activity(
    val title: String,
    val description: String,
    val rating: Double,
    val reviews: Int,
    val address: List<String>,
    val date: String
)

data class RecommendationRequest(
    @SerializedName("check_in") val checkin: String,
    @SerializedName("check_out") val checkout: String,
    @SerializedName("user_profile") val userData: UserData
)

data class UserData(
    val initial_preferences: List<String>,
    val history: List<String>
)

