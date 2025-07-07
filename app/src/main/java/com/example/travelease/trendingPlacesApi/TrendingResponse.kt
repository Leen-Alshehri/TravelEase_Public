package com.example.travelease.trendingPlacesApi

data class TrendingResponse(
    val popular_destinations: PopularDestinations?
)

data class PopularDestinations(
    val destinations: List<TrendingDestination> = emptyList(),
    val show_more_link: String? = null
)

data class TrendingDestination(
    val title: String,
    val link: String,
    val description: String,
    val flight_price: String,
    val hotel_price: String,
    val thumbnail: String
)
data class HighResImageResponse(
    val images_results: List<HighResImageResult> = emptyList()
)

data class HighResImageResult(
    val position: Int,
    val original: String,
    val thumbnail: String
)
