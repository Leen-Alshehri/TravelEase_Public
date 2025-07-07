package com.example.travelease.activityApi

import com.google.gson.annotations.SerializedName// because when in api is a keyword for kotlin


data class ActivitiesResponse(
    val events_results: List<ActivityItem>?
)

data class ActivityItem(
    val title: String,
    val date: ActivityDate,
    val address: List<String>,
    val link: String,
    val description: String,
    val ticket_info: List<TicketInfo>?,
    val venue: Venue?,
    @SerializedName("event_location_map")
    val eventLocationMap: EventLocationMap?,
    val thumbnail: String?
)
data class ActivityDate(
    @SerializedName("start_date") val startDate: String,
    @SerializedName("when") val eventTime: String
)

data class TicketInfo(
    val source: String,
    val link: String,
    val link_type: String
)

data class Venue(
    val name: String,
    val rating: Double,
    val reviews: Int,
    val link: String
)

data class EventLocationMap(
    val image: String?,
    val link: String?,
    val serpapi_link: String?
)