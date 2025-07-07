package com.example.travelease.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.AuthViewModel
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.pages.AccommodationScreen
import com.example.travelease.pages.ActivityScreen
import com.example.travelease.pages.ChatbotPage
import com.example.travelease.pages.FlightScreen
import com.example.travelease.pages.HomePage
import com.example.travelease.pages.ProfilePage
import com.example.travelease.pages.SearchPage
import com.example.travelease.pages.SocialPage
import com.example.travelease.pages.TripPage
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.trendingPlacesApi.TrendingViewModel
import com.example.travelease.pages.CommentsPage
import com.example.travelease.pages.ItineraryPage
import com.google.firebase.auth.FirebaseAuth
import com.example.travelease.pages.CreatePostScreen
import com.example.travelease.pages.RecommendationPage
import com.example.travelease.pages.SelectionPage

@Composable
fun MainNavGraph(modifier: Modifier,
                 navController: NavHostController,
                 rootNavController: NavController,
                 authViewModel: AuthViewModel,
                 travelViewModel: dbViewModel,
                 mainViewModel: MainViewModel,
                 accommodationViewModel: AccommodationViewModel,
                 activitiesViewModel: ActivitiesViewModel,
                 recommendationViewModel: RecommendationViewModel) {

    NavHost(
        navController = navController,
        route = Graph.MAIN,
        startDestination = BottomBarScreens.Home.route
    ) {
        composable(route = BottomBarScreens.Home.route) {
            val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "INVALID_ID"
            val trendingViewModel: TrendingViewModel = viewModel()

            HomePage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                travelerId = travelerId,
                dbViewModel = travelViewModel,
                trendingViewModel = trendingViewModel
            )
        }



        composable(route = BottomBarScreens.Trip.route) {
            TripPage(modifier, navController, travelViewModel, recommendationViewModel)
        }
        composable(Graph.SELECTION) {
            SelectionPage(modifier=modifier, parentNavController = navController, viewModel = travelViewModel)
        }

        composable(route = BottomBarScreens.Search.route) {
            SearchPage(
                modifier,
                navController,
                mainViewModel,
                accommodationViewModel,
                activitiesViewModel,
                tabIndex = 0,
                tripId = "",
                itineraryId = "",
                dbViewModel =travelViewModel,
                mode="selectTrip"
            )
        }
        composable(route = BottomBarScreens.Social.route){
            SocialPage(modifier,navController, travelViewModel)
        }
        composable(route = Screens.MyPosts.route){
            SocialPage(modifier, navController, travelViewModel)
        }
        composable(route = BottomBarScreens.Chatbot.route) {
            ChatbotPage(modifier, navController)
        }
        composable(route = Screens.Profile.route) {
            ProfilePage(modifier, navController, authViewModel, travelViewModel, rootNavController)
        }

        composable(route = Screens.Recommendation.route){
            RecommendationPage(viewModel = recommendationViewModel, navController = navController,
                dbViewModel = travelViewModel)
        }

        composable(
            route = "flights_search?tabIndex={tabIndex}&tripId={tripId}&itineraryId={itineraryId}",
            arguments = listOf(
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 },
                navArgument("tripId") { type = NavType.StringType },
                navArgument("itineraryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            val tripId = backStackEntry.arguments?.getString("tripId") ?: "INVALID_ID"
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: "INVALID_ID"

            SearchPage(
                navController = navController,
                viewModel = mainViewModel,
                accommodationViewModel = accommodationViewModel,
                activitiesViewModel = activitiesViewModel,
                tabIndex = tabIndex,
                tripId = tripId,
                itineraryId = itineraryId, dbViewModel = travelViewModel, mode="add"
            )
        }

        // new
        composable(
            route = "accommodation_screen?tripId={tripId}&itineraryId={itineraryId}&mode={mode}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType; defaultValue = "null" },
                navArgument("itineraryId") { type = NavType.StringType; defaultValue = "null" },
                navArgument("mode") { type = NavType.StringType; defaultValue = "selectTrip" }
            )
        ) { backStackEntry ->
            val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "INVALID_ID"
            val tripId = backStackEntry.arguments?.getString("tripId")?.takeIf { it != "null" }
            val itineraryId = backStackEntry.arguments?.getString("itineraryId")?.takeIf { it != "null" }
            val mode = backStackEntry.arguments?.getString("mode") ?: "selectTrip"

            AccommodationScreen(
                viewModel = accommodationViewModel,
                navController = navController,
                travelerId = travelerId,
                tripId = tripId ?: "INVALID_ID",
                itineraryId = itineraryId ?: "INVALID_ID",
                dbViewModel = travelViewModel,
                mode = mode
            )
        }


        composable(
            route = "accommodations_search?tabIndex={tabIndex}&tripId={tripId}&itineraryId={itineraryId}",
            arguments = listOf(
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 1 },
                navArgument("tripId") { type = NavType.StringType },
                navArgument("itineraryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 1
            val tripId = backStackEntry.arguments?.getString("tripId") ?: "INVALID_ID"
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: "INVALID_ID"

            SearchPage(
                navController = navController,
                viewModel = mainViewModel,
                accommodationViewModel = accommodationViewModel,
                activitiesViewModel = activitiesViewModel,
                tabIndex = tabIndex,
                tripId = tripId,
                itineraryId = itineraryId, dbViewModel = travelViewModel, mode="add"
            )
        }


        //------Activity--------------------

        composable(route="activity_screen/{tripId}/{itineraryId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType },
                navArgument("itineraryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "INVALID_ID"
            val tripId = backStackEntry.arguments?.getString("tripId") ?: "INVALID_ID"
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: "INVALID_ID"

            Log.d("NavigationArgs", "Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId")

            ActivityScreen(activitiesViewModel,navController, travelerId, tripId, itineraryId, travelViewModel)
        }

        composable(
            route = "activities_search?tabIndex={tabIndex}&tripId={tripId}&itineraryId={itineraryId}",
            arguments = listOf(
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 2 },
                navArgument("tripId") { type = NavType.StringType },
                navArgument("itineraryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 2
            val tripId = backStackEntry.arguments?.getString("tripId") ?: "INVALID_ID"
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: "INVALID_ID"

            SearchPage(
                navController = navController,
                viewModel = mainViewModel,
                accommodationViewModel = accommodationViewModel,
                activitiesViewModel = activitiesViewModel,
                tabIndex = tabIndex,
                tripId = tripId,
                itineraryId = itineraryId, dbViewModel = travelViewModel, mode="add"
            )
        }


        composable(route = "${Screens.Comments.route}/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentsPage(
                navController = navController, username = "", postId = postId,
                dbViewModel = travelViewModel
            )
        }


        composable(route = Screens.CreatePost.route){
            CreatePostScreen(navController,travelViewModel)
        }

        composable(route = "itinerary/{tripId}/{itineraryId}/{tripName}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: ""
            val tripName = backStackEntry.arguments?.getString("tripName") ?: "Trip Itinerary"

            ItineraryPage(
                navController = navController,
                tripId = tripId,
                itineraryId = itineraryId,
                tripName = tripName,
                dbViewModel = travelViewModel
            )
        }

        composable(
            route = "flight_screen?tripId={tripId}&itineraryId={itineraryId}&mode={mode}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType; defaultValue = "null" },
                navArgument("itineraryId") { type = NavType.StringType; defaultValue = "null" },
                navArgument("mode") { type = NavType.StringType; defaultValue = "selectTrip" }
            )
        ) { backStackEntry ->
            val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "INVALID_ID"
            val tripId = backStackEntry.arguments?.getString("tripId")?.takeIf { it != "null" }
            val itineraryId = backStackEntry.arguments?.getString("itineraryId")?.takeIf { it != "null" }
            val mode = backStackEntry.arguments?.getString("mode") ?: "selectTrip"

            FlightScreen(
                viewModel = mainViewModel,
                navController = navController,
                travelerId = travelerId,
                tripId = tripId,
                itineraryId = itineraryId,
                dbViewModel = travelViewModel,
                mode = mode
            )
        }

    }
}
