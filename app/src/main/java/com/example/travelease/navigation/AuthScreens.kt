package com.example.travelease.navigation

sealed class AuthScreens(val route: String) {
    object signIn : AuthScreens(route = "signin")
    object signUp : AuthScreens(route = "signup")
    object forgotPassword: AuthScreens(route = "forgotPassword")
}