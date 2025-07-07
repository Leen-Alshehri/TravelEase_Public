package com.example.travelease.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.travelease.AuthViewModel
import com.example.travelease.pages.SignInPage
import com.example.travelease.pages.SignUpPage
import com.example.travelease.pages.ForgotPasswordPage
fun NavGraphBuilder.authNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = AuthScreens.signIn.route,
        route = Graph.AUTHENTICATION
    ) {
        composable(route = AuthScreens.signIn.route) {
            SignInPage(modifier, navController, authViewModel)
        }
        composable(route = AuthScreens.signUp.route) {
            SignUpPage(modifier, navController, authViewModel)
        }
        composable(route = AuthScreens.forgotPassword.route){
            ForgotPasswordPage(modifier, navController, authViewModel)
        }
    }
}
