package com.example.travelease.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.AuthViewModel
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Orange


@Composable
fun MainScreen(modifier: Modifier = Modifier,
               authViewModel: AuthViewModel,
               navController: NavHostController= rememberNavController(),
               rootNavController: NavController,
               mainViewModel: MainViewModel,
               travelViewModel: dbViewModel,
               accommodationViewModel: AccommodationViewModel,
               activitiesViewModel : ActivitiesViewModel,
               recommendationViewModel: RecommendationViewModel
) {

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            // You should call your NavHost or main content here
            MainNavGraph(
                modifier = modifier,
                authViewModel = authViewModel,
                navController = navController,
                rootNavController = rootNavController,
                mainViewModel = mainViewModel,
                travelViewModel = travelViewModel,
                accommodationViewModel = accommodationViewModel,
                activitiesViewModel = activitiesViewModel,
                recommendationViewModel = recommendationViewModel
            )
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreens.Home,
        BottomBarScreens.Trip,
        BottomBarScreens.Search,
        BottomBarScreens.Social,
        BottomBarScreens.Chatbot
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (bottomBarDestination) {
        NavigationBar( modifier = Modifier.shadow(elevation = 0.9.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            ambientColor = Color.Black),
            containerColor = Color.White,
            tonalElevation = 2.dp
        ) {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreens,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val selected=currentRoute == screen.route
    BottomNavigationItem(
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "Navigation Icon",
                tint = if (selected) Orange else Color.Gray,
                // modifier=Modifier.size(30.dp)
            )
        },
        label = {
            Text(
                text = screen.title,
                fontSize = 10.sp,
                color = if (selected) Orange else Color.Gray,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal
            )
            // }
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}