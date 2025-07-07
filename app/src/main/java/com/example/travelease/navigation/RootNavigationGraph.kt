package com.example.travelease.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.AuthViewModel
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.pages.SelectionPage


@Composable
fun RootNavigationGraph(modifier: Modifier,
                        authViewModel: AuthViewModel,
                        mainViewModel: MainViewModel,
                        accommodationViewModel: AccommodationViewModel,
                        activitiesViewModel: ActivitiesViewModel,
                        travelViewModel: dbViewModel,
                        recommendationViewModel: RecommendationViewModel) {
    val navController= rememberNavController()
    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = Graph.AUTHENTICATION
    ) {
        authNavGraph(modifier, navController=navController, authViewModel)
        composable(route=Graph.MAIN){
            MainScreen(modifier,authViewModel, mainViewModel = mainViewModel,
                travelViewModel = travelViewModel,
                accommodationViewModel = accommodationViewModel,
                activitiesViewModel= activitiesViewModel,
                recommendationViewModel = recommendationViewModel,
                rootNavController = navController)
        }
        composable(route=Graph.SELECTION){
            SelectionPage(modifier,parentNavController = navController, viewModel = travelViewModel)
        }

    }

}

object Graph{
    const val ROOT = "root_graph"
    const val AUTHENTICATION = "auth_graph"
    const val MAIN = "main_graph"
    const val SELECTION = "selection_flow"
}