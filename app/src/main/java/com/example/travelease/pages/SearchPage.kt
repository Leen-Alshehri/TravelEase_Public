package com.example.travelease.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MainViewModel,
    accommodationViewModel: AccommodationViewModel,
    activitiesViewModel: ActivitiesViewModel,
    tabIndex: Int = 0,
    tripId: String="",
    itineraryId: String="",
    dbViewModel: dbViewModel,
    mode: String = "selectTrip"
) {
    val selectedTabIndex = remember { mutableIntStateOf(tabIndex) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Search", fontFamily = alefFontFamily) },
                navigationIcon = {
                    if (mode != "selectTrip") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
            )
        },
        containerColor = Color.White
    ) { values ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
                .background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchTabs(selectedTabIndex, navController, viewModel, accommodationViewModel, activitiesViewModel, tripId, itineraryId,dbViewModel, mode)
        }
    }
}





@Composable
fun SearchTabs(
    selectedTabIndex: MutableState<Int>,
    navController: NavController,
    viewModel: MainViewModel,
    accommodationViewModel: AccommodationViewModel,
    activitiesViewModel: ActivitiesViewModel,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel,
    mode: String
) {
    val tabTitles = listOf("Flights", "Accommodation", "Activities")

    TabRow(
        selectedTabIndex = selectedTabIndex.value,
        containerColor = Color.White,
        contentColor = Color.Black,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                color = OceanBlue,
                height = 3.dp,
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.value])
            )
        }
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex.value == index,
                onClick = { selectedTabIndex.value = index },
                text = {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = if (selectedTabIndex.value == index) Orange else Color.Gray
                    )
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTabIndex.value) {
        0 -> FlightSearchSection(
            navController, viewModel = viewModel,
            travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            tripId = tripId,
            itineraryId = itineraryId
        )
        1 -> AccommodationSearchSection(
            navController,
            viewModel = accommodationViewModel,
            travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            tripId = tripId,
            itineraryId = itineraryId,
            mode = mode
        )

        2 -> ActivitiesSearchSection(
            navController, viewModel = activitiesViewModel,
            travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            tripId = tripId,
            itineraryId = itineraryId,
            dbViewModel = dbViewModel
        )
    }
}
