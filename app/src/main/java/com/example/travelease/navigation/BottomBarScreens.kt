package com.example.travelease.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreens(
    val route: String,
    val title:String,
    val icon: ImageVector
) {
    object Home: BottomBarScreens(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )
    object Trip: BottomBarScreens(
        route = "trip",
        title = "Trip",
        icon = Icons.Default.AirplanemodeActive
    )
    object Search: BottomBarScreens(
        route = "search",
        title = "Search",
        icon = Icons.Default.Search
    )
    object Social: BottomBarScreens(
        route = "social",
        title = "Social",
        icon = Icons.Default.People
    )
    object Chatbot: BottomBarScreens(
        route = "chatbot",
        title = "Chatbot",
        icon = Icons.Filled.ChatBubble
    )
    
}
