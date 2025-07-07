package com.example.travelease.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.travelease.AuthState
import com.example.travelease.AuthViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Accommodation
import com.example.travelease.firebaseDB.entities.Activity
import com.example.travelease.firebaseDB.entities.Flight
import com.example.travelease.firebaseDB.entities.Trip
import com.example.travelease.R
import com.example.travelease.trendingPlacesApi.TrendingDestination
import com.example.travelease.trendingPlacesApi.TrendingViewModel
import com.example.travelease.navigation.BottomBarScreens
import com.example.travelease.navigation.Graph
import com.example.travelease.navigation.Screens
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.alefFontFamily
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelease.weatherNotifications.WeatherViewModel
import androidx.core.content.ContextCompat
import com.example.travelease.weatherNotifications.WeatherResponse
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travelease.weatherNotifications.WeatherWorker
import java.util.concurrent.TimeUnit
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.work.OneTimeWorkRequestBuilder



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    travelerId: String,
    dbViewModel: dbViewModel,
    trendingViewModel: TrendingViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val currentTrips by dbViewModel.currentTrips.collectAsState()
    val noResults by trendingViewModel.noResults.collectAsState()
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var tripMenuExpanded by remember { mutableStateOf(false) }
    var itineraryMenuExpanded by remember { mutableStateOf(false) }
    val trendingDestinations by trendingViewModel.destinations.collectAsState()
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf("France") }
    val todayFlights = remember { mutableStateListOf<Flight>() }
    val tomorrowFlights = remember { mutableStateListOf<Flight>() }
    val todayAccommodations = remember { mutableStateListOf<Accommodation>() }
    val tomorrowAccommodations = remember { mutableStateListOf<Accommodation>() }
    val todayActivities = remember { mutableStateListOf<Activity>() }
    val tomorrowActivities = remember { mutableStateListOf<Activity>() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
        val hasLaunchedOnce = prefs.getBoolean("started_once", false)

        if (!hasLaunchedOnce) {
            // show notification
            val oneTimeRequest = OneTimeWorkRequestBuilder<WeatherWorker>().build()
            WorkManager.getInstance(context).enqueue(oneTimeRequest)
            prefs.edit().putBoolean("started_once", true).apply()
        }
        // Schedule periodic check
        val periodicRequest = PeriodicWorkRequestBuilder<WeatherWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeatherNotificationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }


    val apiKey = "918b3d362653eb54fe52a9b21dbc5611"
    val weatherViewModel: WeatherViewModel = viewModel()
    var temperature by remember { mutableStateOf<Float?>(null) }
    var icon by remember { mutableStateOf<String?>(null) }
    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }

    val weatherEmoji = when (weatherResponse?.weather?.firstOrNull()?.main?.lowercase()) {
        "clear" -> "☀️"
        "clouds" -> "☁️"
        "rain" -> "🌧️"
        "drizzle" -> "🌦️"
        "thunderstorm" -> "⛈️"
        "snow" -> "❄️"
        "mist", "fog", "haze" -> "🌫️"
        else -> "🌍"
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            weatherViewModel.fetchWeather(context, apiKey) { response ->
                response?.let {
                    temperature = it.main.temp
                    icon = it.weather.firstOrNull()?.icon
                    weatherResponse = it
                    Log.d("Weather", "Temp: ${it.main.temp} °C in ${it.name}")
                }
            }
        } else {
            Log.e("Weather", "Permission denied")
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            weatherViewModel.fetchWeather(context, apiKey) { response ->
                response?.let {
                    temperature = it.main.temp
                    icon = it.weather.firstOrNull()?.icon
                    weatherResponse = it
                    Log.d("Weather", "Temp: ${it.main.temp} °C in ${it.name}")
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    val fromToList = listOf(
        "Italy", "Maldives", "Japan", "France", "Iceland"
    )

    LaunchedEffect(currentTrips) {
        if (currentTrips.isNotEmpty() && selectedTrip == null) {
            selectedTrip = currentTrips.first()
        }
    }


    LaunchedEffect(Unit) {
        dbViewModel.fetchTrips(travelerId)
        trendingViewModel.fetchTrendingPlaces(selectedCountry)
    }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(Graph.AUTHENTICATION)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.small_logo),
                                contentDescription = "small logo",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "TravelEase", fontFamily = alefFontFamily)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screens.Profile.route) }) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(25.dp),
                                    tint = Color.Black
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    if (currentTrips.isEmpty()) {
                        NoCurrentTripMessage(navController)
                    }
                    else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Trip Dropdown
                            TextButton(
                                onClick = {tripMenuExpanded = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = selectedTrip?.name ?: "Loading trip...",
                                    color = Color.Black,
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            DropdownMenu(
                                expanded = tripMenuExpanded,
                                onDismissRequest = { tripMenuExpanded = false },
                                //modifier = Modifier.width(160.dp).height(200.dp),
                                containerColor = Color.White
                            ) {
                                currentTrips.forEach { trip ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = trip.name,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(),
                                                fontFamily = DMSansFontFamily,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        onClick = {
                                            selectedTrip = trip.copy()
                                            tripMenuExpanded = false
                                            Log.d("TripDebug", "Selected trip: ${trip.name}, ${trip.tripId}")
                                        }
                                    )
                                }
                            }


                            Box(modifier = Modifier.width(160.dp)) {
                                OutlinedButton(
                                    onClick = { itineraryMenuExpanded = true },
                                    shape = RoundedCornerShape(12),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Add to Itinerary",
                                        fontFamily = DMSansFontFamily,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }

                                DropdownMenu(
                                    expanded = itineraryMenuExpanded,
                                    onDismissRequest = { itineraryMenuExpanded = false },
                                    modifier = Modifier.width(160.dp),
                                    containerColor = Color.White
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Add Flight", textAlign = TextAlign.Center,
                                            fontSize = 12.sp) },
                                        onClick = {
                                            selectedTrip?.let {
                                                val tripId = it.tripId ?: ""
                                                val itineraryId = it.itineraryId ?: ""
                                                navController.navigate("flights_search?tabIndex=0&tripId=$tripId&itineraryId=$itineraryId")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add Accommodation",
                                           /* textAlign = TextAlign.Center*/ fontSize = 12.sp) },
                                        onClick = {
                                            selectedTrip?.let {
                                                val tripId = it.tripId ?: ""
                                                val itineraryId = it.itineraryId ?: ""
                                                navController.navigate("accommodations_search?tabIndex=1&tripId=$tripId&itineraryId=$itineraryId")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add Activity",textAlign = TextAlign.Center
                                        , fontSize = 12.sp) },
                                        onClick = {
                                            selectedTrip?.let {
                                                val tripId = it.tripId ?: ""
                                                val itineraryId = it.itineraryId ?: ""
                                                navController.navigate("activities_search?tabIndex=2&tripId=$tripId&itineraryId=$itineraryId")
                                            }
                                        }
                                    )
                                }
                            }

                        }
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            color = Color.LightGray,
                            thickness = 0.8.dp
                        )

                        val todayFormatted = remember {
                            SimpleDateFormat("EEE, d/M", Locale.US).format(Date())
                        }
                        val tomorrowFormatted = remember {
                            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
                        }.let {
                            SimpleDateFormat("EEE, d/M", Locale.US).format(it)
                        }
                        val travelerId = FirebaseAuth.getInstance().currentUser?.uid
                        val todayDate =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        val tomorrowDate =
                            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                                .let {
                                    SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.getDefault()
                                    ).format(it.time)
                                }

                        LaunchedEffect(selectedTrip) {
                            if (selectedTrip != null && travelerId != null) {
                                val tripId = selectedTrip!!.tripId
                                val itineraryId = selectedTrip!!.itineraryId

                                dbViewModel.getFlightsByItinerary(
                                    travelerId,
                                    tripId,
                                    itineraryId
                                ) { all ->
                                    todayFlights.clear(); tomorrowFlights.clear()
                                    all.forEach { flight ->
                                        val flightDate =
                                            flight.flightDate
                                        when (flightDate) {
                                            todayDate -> todayFlights.add(flight)
                                            tomorrowDate -> tomorrowFlights.add(flight)
                                        }
                                    }
                                }

                                dbViewModel.getAccommodationsByItinerary(
                                    travelerId,
                                    tripId,
                                    itineraryId
                                ) { all ->
                                    todayAccommodations.clear()
                                    tomorrowAccommodations.clear()

                                    all.forEach { acc ->
                                        if (acc.checkIn == todayDate || acc.checkOut == todayDate) {
                                            todayAccommodations.add(
                                                acc.copy(description = if (acc.checkIn == todayDate) "Check-In" else "Check-Out")
                                            )
                                        }
                                        if (acc.checkIn == tomorrowDate || acc.checkOut == tomorrowDate) {
                                            tomorrowAccommodations.add(
                                                acc.copy(description = if (acc.checkIn == tomorrowDate) "Check-In" else "Check-Out")
                                            )
                                        }

                                    }

                                }

                                dbViewModel.getActivitiesByItinerary(
                                    travelerId,
                                    tripId,
                                    itineraryId
                                ) { all ->
                                    todayActivities.clear()
                                    tomorrowActivities.clear()
                                    all.forEach { act ->
                                        when {
                                            matchesTodayOrTomorrow(
                                                act.sdate,
                                                true
                                            ) -> todayActivities.add(act)

                                            matchesTodayOrTomorrow(
                                                act.sdate,
                                                false
                                            ) -> tomorrowActivities.add(act)
                                        }
                                    }
                                }
                            }
                        }
                        if (
                            todayFlights.isNotEmpty() || todayAccommodations.isNotEmpty() || todayActivities.isNotEmpty() ||
                            tomorrowFlights.isNotEmpty() || tomorrowAccommodations.isNotEmpty() || tomorrowActivities.isNotEmpty()
                        ) {
                            Column(modifier = Modifier.padding(start = 17.dp, top = 12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = todayFormatted,
                                        fontFamily = DMSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = weatherEmoji,
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Column(horizontalAlignment = Alignment.Start) {
                                            temperature?.let {
                                                Text(
                                                    text = "${it}°C",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            weatherResponse?.name?.let { city ->
                                                Text(
                                                    text = city,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }



                                if (todayFlights.isEmpty() && todayAccommodations.isEmpty() && todayActivities.isEmpty()) {
                                    Text(
                                        "Nothing for today",
                                        modifier = Modifier.padding(start = 17.dp),
                                        color = Color.Gray
                                    )
                                } else {
                                    todayFlights.forEach {
                                        FlightItem(
                                            it,
                                            travelerId!!,
                                            selectedTrip!!.tripId,
                                            selectedTrip!!.itineraryId,
                                            dbViewModel,
                                            onFlightDeleted = { },
                                            showDeleteIcon = false
                                        )
                                    }
                                    todayAccommodations.forEach {
                                        AccommodationItem(
                                            accommodation = it,
                                            travelerId = travelerId!!,
                                            tripId = selectedTrip!!.tripId,
                                            itineraryId = selectedTrip!!.itineraryId,
                                            dbViewModel = dbViewModel,
                                            onAccommodationDeleted = { },
                                            showDeleteIcon = false,
                                            showCheckInOutFlag = true
                                        )
                                    }
                                    todayActivities.forEach {
                                        ActivityItem(
                                            it,
                                            travelerId!!,
                                            selectedTrip!!.tripId,
                                            selectedTrip!!.itineraryId,
                                            dbViewModel,
                                            onActivityDeleted = { },
                                            showDeleteIcon = false
                                        )
                                    }
                                }


                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = tomorrowFormatted,
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(start = 17.dp)
                                )

                                if (tomorrowFlights.isEmpty() && tomorrowAccommodations.isEmpty() && tomorrowActivities.isEmpty()) {
                                    Text(
                                        "Nothing for tomorrow",
                                        modifier = Modifier.padding(start = 17.dp),
                                        color = Color.Gray
                                    )
                                } else {
                                    tomorrowFlights.forEach {
                                        FlightItem(
                                            it,
                                            travelerId!!,
                                            selectedTrip!!.tripId,
                                            selectedTrip!!.itineraryId,
                                            dbViewModel,
                                            onFlightDeleted = { },
                                            showDeleteIcon = false
                                        )
                                    }
                                    tomorrowAccommodations.forEach {
                                        AccommodationItem(
                                            accommodation = it,
                                            travelerId = travelerId!!,
                                            tripId = selectedTrip!!.tripId,
                                            itineraryId = selectedTrip!!.itineraryId,
                                            dbViewModel = dbViewModel,
                                            onAccommodationDeleted = { },
                                            showDeleteIcon = false,
                                            showCheckInOutFlag = true
                                        )
                                    }

                                    tomorrowActivities.forEach {
                                        ActivityItem(
                                            it,
                                            travelerId!!,
                                            selectedTrip!!.tripId,
                                            selectedTrip!!.itineraryId,
                                            dbViewModel,
                                            onActivityDeleted = { },
                                            showDeleteIcon = false
                                        )
                                    }
                                }
                            }
                        }
                        if (
                            todayFlights.isEmpty() && todayAccommodations.isEmpty() && todayActivities.isEmpty() &&
                            tomorrowFlights.isEmpty() && tomorrowAccommodations.isEmpty() && tomorrowActivities.isEmpty()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "No plans",
                                    modifier = Modifier.size(100.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "You have no plans yet!",
                                    fontFamily = DMSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                TextButton(onClick = { itineraryMenuExpanded = true }) {
                                    Text("Add something fun?", color = OceanBlue)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color.LightGray,
                        thickness = 0.8.dp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't know where to go?",
                            fontSize = 18.sp,
                            fontFamily = DMSansFontFamily
                        )
                        Text(
                            text = " Click here",
                            fontSize = 18.sp,
                            fontFamily = DMSansFontFamily,
                            // fontWeight = FontWeight.Bold,
                            color = OceanBlue,
                            modifier = Modifier.clickable { countryMenuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = countryMenuExpanded,
                            onDismissRequest = { countryMenuExpanded = false },
                            containerColor = Color.White
                        ) {
                            fromToList.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(country) },
                                    onClick = {
                                        selectedCountry = country
                                        countryMenuExpanded = false
                                        trendingViewModel.fetchTrendingPlaces(country)
                                    }
                                )
                            }
                        }
                    }

                    if (noResults) {
                        Text(
                            text = "No trending destinations found for your query.",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    } else {
                        TrendingPlacesRow(destinations = trendingDestinations, TrendingViewModel = trendingViewModel)
                    }
                }
            }
        }
    }
}

fun matchesTodayOrTomorrow(activityDate: String?, isToday: Boolean): Boolean {
    if (activityDate.isNullOrEmpty()) return false

    val today = Calendar.getInstance()
    if (!isToday) today.add(Calendar.DAY_OF_YEAR, 1)

    // Normalize time for comparison
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val formats = listOf("dd MMM yyyy", "MMM dd", "yyyy-MM-dd", "MMM d")

    for (format in formats) {
        try {
            val parsed = SimpleDateFormat(format, Locale.ENGLISH).parse(activityDate)
            val cal = Calendar.getInstance().apply {
                time = parsed!!
                if (format == "MMM d" || format == "MMM dd") {
                    set(Calendar.YEAR, today.get(Calendar.YEAR))
                }
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (cal.time == today.time) return true
        } catch (_: Exception) {
            continue
        }
    }

    return false
}



@Composable
fun TrendingPlacesRow(destinations: List<TrendingDestination>, TrendingViewModel: TrendingViewModel) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Trending Destinations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            val normalizedExcludes = listOf("dhaalu atoll", "dhaalu atol", "lhaviyani atoll", "lhaviyani", "hiroshima", "provence", "provence-alpes")

            items(destinations.filter {
                val normTitle = it.title.lowercase().replace("-", " ").replace("_", " ").replace(",", " ").replace("(", " ").replace(")", " ").trim()
                normalizedExcludes.none { excluded -> normTitle.contains(excluded) }
            }) { destination ->


            Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(230.dp)
                        .clickable { uriHandler.openUri(destination.link) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    var imageUrl by remember { mutableStateOf(destination.thumbnail) }

                    LaunchedEffect(destination.title) {
                        TrendingViewModel.fetchHighQualityImage(destination.title) { url ->
                            if (url.isNotEmpty()) {
                                imageUrl = url
                            }
                        }
                    }

                    Column {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = destination.title,
                            modifier = Modifier
                                .height(130.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(destination.title, fontWeight = FontWeight.Bold)
                            Text(destination.description, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("✈️ ${destination.flight_price}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoCurrentTripMessage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = "No trip",
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No trips? No worries!",
            fontFamily = DMSansFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You don't have a current trip yet.\n" +
                    "Let's start planning your next adventure!",
            fontFamily = DMSansFontFamily,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = {
                navController.navigate(BottomBarScreens.Trip.route)
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OceanBlue),
            border = BorderStroke(1.5.dp, OceanBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Create a Trip",
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}






