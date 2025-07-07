package com.example.travelease.navigation

sealed class Screens(
    val route:String,
    val title:String) {
    object Profile:Screens(
        route = "profile",
        title = "Profile"
    )
    object Flights:Screens(
        route = "flights?travelerId={travelerId}&tripId={tripId}&itineraryId={itineraryId}",
        title = "Flights"
    )

    object FlightsSearch:Screens(
        route = "flightsSearch",
        title = "FlightsSearch"
    )
    object Accommodations:Screens(
        route = "accommodations?travelerId={travelerId}&tripId={tripId}&itineraryId={itineraryId}",
        title = "Accommodations"
    )
    object AccommodationsSearch:Screens(
        route = "accommodations_search"//?tabIndex={tabIndex}&tripId={tripId}&itineraryId={itineraryId}",//"accommodationsSearch",
        ,title = "AccommodationsSearch"
    )
    object Activities:Screens(
        route = "activities?travelerId={travelerId}&tripId={tripId}&itineraryId={itineraryId}",
        title = "Activities"
    )
    object ActivitiesSearch:Screens(
        route = "activities_search"//?tabIndex={tabIndex}&tripId={tripId}&itineraryId={itineraryId}",//"activitiesSearch",
        ,title = "ActivitiesSearch"
    )
    object Comments:Screens(
        route = "comments/{postId}",
        title = "Comments"
    )
    object CreatePost:Screens(
        route = "createPost",
        title = "CreatePost"
    )
    object MyPosts:Screens(
        route = "myPosts",
        title = "MyPosts"
    )
    object Recommendation:Screens(
        route = "recommendation",
        title = "Recommendation"
    )

}
