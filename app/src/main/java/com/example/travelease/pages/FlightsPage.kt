package com.example.travelease.pages

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import com.airbnb.lottie.compose.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.flightsApi.Layover
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Flight
import com.example.travelease.firebaseDB.entities.FlightSegment
import com.example.travelease.flightsApi.BestFlight
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.travelease.firebaseDB.entities.Trip


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightScreen(
    viewModel: MainViewModel,
    navController: NavController,
    travelerId: String,
    tripId: String?,             //
    itineraryId: String?,        //both nullable for navigation reasons
    dbViewModel: dbViewModel,
    mode: String                 //"specificTrip" or "selectTrip"
) {
    val flights by viewModel.flights
    val errorMessage by viewModel.errorMessage
    val sortBy = viewModel.sortBy
    //val travelerId = FirebaseAuth.getInstance().currentUser?.uid

    Log.d(
        "FlightScreen",
        "Received IDs: Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId"
    )

    if (travelerId == "INVALID_ID" || tripId == "INVALID_ID" || itineraryId == "INVALID_ID") {
        Log.e("FlightScreen", "ERROR: Invalid Navigation IDs!")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Available Flights")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { values ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(values)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flights from ${viewModel.fromText.value} to ${viewModel.toText.value}",
                    fontWeight = FontWeight.SemiBold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                SortDropdownMenu(selectedSort = sortBy.value, onSortSelected = { newSort ->
                    viewModel.updateSortBy(newSort)
                })
            }

            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Oops!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }


            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(flights) { bestFlight ->
                    if (mode == "specificTrip" && tripId != null && itineraryId != null) {
                        FlightCard(
                            flight = bestFlight,
                            travelerId = travelerId,
                            tripId = tripId,
                            itineraryId = itineraryId,
                            dbViewModel = dbViewModel
                        )
                    } else {
                        FlightCardWithTripPicker(
                            flight = bestFlight,
                            travelerId = travelerId,
                            dbViewModel = dbViewModel
                        )
                    }
                }

            }

        }
    }
}

@Composable
fun FlightCard(
    flight: BestFlight,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Logo + Airline Name + Travel Class
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    flight.flights.first().airline_logo?.let { logoUrl ->
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Airline Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = flight.flights.first().airline,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = flight.flights.first().travel_class ?: "",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${flight.flights.first().flight_number} • ${flight.flights.first().airplane}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${flight.flights.first().departure_airport.id} → ${flight.flights.last().arrival_airport.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (flight.isDirect) "Direct" else "Non-Direct",
                    fontWeight = FontWeight.Bold,
                    color = if (flight.isDirect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            flight.flights.forEachIndexed { index, segment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val timeText = "${segment.departure_airport.time.substring(11)} - ${
                        segment.arrival_airport.time.substring(11)
                    }"
                    val delayedText =
                        if (segment.often_delayed_by_over_30_min == true) " ⚠️ Often Delayed" else ""
                    Text("$timeText$delayedText")
                    Text(segment.airline)
                }

                if (index < flight.flights.size - 1) {
                    val layover = flight.layovers?.getOrNull(index)
                    layover?.let { LayoverInfo(it) }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            flight.flights.first().extensions?.takeIf { it.isNotEmpty() }?.let { extensions ->
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    extensions.forEach { ext ->
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(ext, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${flight.price} SAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Flight")
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                if (!showSuccessAnimation) {
                    Text(
                        text = "Add Flight?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFFF6421),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                if (showSuccessAnimation) {
                    // Show Success Animation + Text Below it
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimation(
                            modifier = Modifier.size(250.dp), // Size of Success Animation
                            animationFile = "success_check.json"
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Space below animation

                        // Text Below Success Animation
                        Text(
                            text = "Added Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,//(0xFF0C3D8D), // ocean blue Success Color
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to add this flight to your trip?",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!showSuccessAnimation) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                showSuccessAnimation = true

                                val flightData = Flight(
                                    flightNo = flight.flights.first().flight_number,
                                    itineraryId = "",
                                    from = flight.flights.first().departure_airport.id,
                                    to = flight.flights.last().arrival_airport.id,
                                    departureTime = flight.flights.first().departure_airport.time,
                                    arrivalTime = flight.flights.last().arrival_airport.time,
                                    flightId = UUID.randomUUID().toString(),
                                    price = flight.price * 3.75,
                                    direct = if (flight.isDirect) "Direct" else "Non-Direct",
                                    airline = flight.flights.first().airline,
                                    duration = flight.flights.first().duration,
                                    layovers = flight.layovers,
                                    segments = flight.flights.map {
                                        FlightSegment(
                                            departure_airport = it.departure_airport,
                                            arrival_airport = it.arrival_airport,
                                            duration = it.duration,
                                            airline = it.airline,
                                            flight_number = it.flight_number
                                        )
                                    },
                                    flightDate = flight.flights.first().departure_airport.time.substring(
                                        0,
                                        10
                                    )
                                )

                                dbViewModel.addFlightToItinerary(
                                    travelerId,
                                    itineraryId = itineraryId,
                                    flight = flightData,
                                    onSuccess = {
                                        Log.d(
                                            "Firestore",
                                            "Flight added successfully!"
                                        )
                                    },
                                    onFailure = { e -> Log.e("Firestore", "Error: ${e.message}") },
                                    tripId = tripId
                                )

                                Log.d(
                                    "Firestore",
                                    "Flight added successfully for traveler $travelerId"
                                )

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(2500)
                                    showDialog = false
                                    showSuccessAnimation = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("Add Flight", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FlightCardWithTripPicker(
    flight: BestFlight,
    travelerId: String,
    dbViewModel: dbViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var expanded by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    val currentTrips by dbViewModel.currentTrips.collectAsState()
    val upcomingTrips by dbViewModel.upcomingTrips.collectAsState()

    LaunchedEffect(travelerId) {
        dbViewModel.fetchTrips(travelerId)
    }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        flight.flights.first().airline_logo?.let { logoUrl ->
                            AsyncImage(
                                model = logoUrl,
                                contentDescription = "Airline Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = flight.flights.first().airline,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = flight.flights.first().travel_class ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${flight.flights.first().flight_number} • ${flight.flights.first().airplane}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${flight.flights.first().departure_airport.id} → ${flight.flights.last().arrival_airport.id}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (flight.isDirect) "Direct" else "Non-Direct",
                        fontWeight = FontWeight.Bold,
                        color = if (flight.isDirect) Color(0xFF0C3D8D) else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Segments + delay indicator
                flight.flights.forEachIndexed { index, segment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val timeText = "${segment.departure_airport.time.substring(11)} - ${
                            segment.arrival_airport.time.substring(11)
                        }"
                        val delayedText =
                            if (segment.often_delayed_by_over_30_min == true) " ⚠️ Often Delayed" else ""
                        Text("$timeText$delayedText")
                        Text(segment.airline)
                    }

                    if (index < flight.flights.size - 1) {
                        val layover = flight.layovers?.getOrNull(index)
                        layover?.let { LayoverInfo(it) }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                flight.flights.first().extensions?.takeIf { it.isNotEmpty() }?.let { extensions ->
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        extensions.forEach { ext ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(ext, fontSize = 12.sp)
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // Price and add icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${flight.price} SAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add to trip")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = {Text("Choose a trip", fontWeight = FontWeight.Bold)},
                                onClick = {}
                            )
                            (currentTrips + upcomingTrips).forEach { trip ->
                                DropdownMenuItem(
                                    text = { Text(trip.name) },
                                    onClick = {
                                        selectedTrip = trip
                                        expanded = false
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { snackbarData ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    color = Color.White,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFFFF6421),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = snackbarData.visuals.message,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Confirmation Dialog
    if (showDialog && selectedTrip != null) {
        val trip = selectedTrip!!

        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                if (!showSuccessAnimation) {
                    Text(
                        text = "Add Flight?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFFF6421),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                if (showSuccessAnimation) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LottieAnimation(
                            modifier = Modifier.size(250.dp),
                            animationFile = "success_check.json"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Added Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add this flight to '${trip.name}'?",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!showSuccessAnimation) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                dbViewModel.getFlightCountForItinerary(
                                    travelerId = travelerId,
                                    tripId = trip.tripId,
                                    itineraryId = trip.itineraryId
                                ) { count ->
                                    if (count >= 2) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Only 2 flights allowed per itinerary!")
                                        }
                                        showDialog = false
                                    } else {
                                        showSuccessAnimation = true


                                        val flightData = Flight(

                                            flightNo = flight.flights.first().flight_number,
                                            itineraryId = trip.itineraryId,
                                            from = flight.flights.first().departure_airport.id,
                                            to = flight.flights.last().arrival_airport.id,
                                            departureTime = flight.flights.first().departure_airport.time,
                                            arrivalTime = flight.flights.last().arrival_airport.time,
                                            flightId = UUID.randomUUID().toString(),
                                            price = flight.price * 3.75,
                                            direct = if (flight.isDirect) "Direct" else "Non-Direct",
                                            airline = flight.flights.first().airline,
                                            duration = flight.flights.first().duration,
                                            layovers = flight.layovers,
                                            segments = flight.flights.map {
                                                FlightSegment(
                                                    departure_airport = it.departure_airport,
                                                    arrival_airport = it.arrival_airport,
                                                    duration = it.duration,
                                                    airline = it.airline,
                                                    flight_number = it.flight_number
                                                )
                                            },
                                            flightDate = flight.flights.first().departure_airport.time.substring(
                                                0,
                                                10
                                            ) // "yyyy-MM-dd"
                                        )

                                        dbViewModel.addFlightToItinerary(
                                            travelerId = travelerId,
                                            tripId = trip.tripId,
                                            itineraryId = trip.itineraryId,
                                            flight = flightData,
                                            onSuccess = {
                                                Log.d("Firestore", "Flight added to ${trip.name}")
                                            },
                                            onFailure = { e ->
                                                Log.e("Firestore", "Error: ${e.message}")
                                            }
                                        )

                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(2500)
                                            showDialog = false
                                            showSuccessAnimation = false
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("Add Flight", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LottieAnimation(modifier: Modifier = Modifier, animationFile: String) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(animationFile))
    val animationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LottieAnimation(
        composition = composition,
        progress = { animationState.progress },
        modifier = modifier
    )
}


@Composable
fun LayoverInfo(layover: Layover) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp, bottom = 4.dp)) {
        Text(
            text = "Layover at ${layover.name} (${layover.id}) - ${layover.duration} min",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (layover.overnight) {
            Text(
                text = "Overnight layover",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SortDropdownMenu(selectedSort: Int, onSortSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Top Flights", "Price", "Departure Time", "Arrival Time", "Duration")

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort Flights")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(label, color = Color.Black) },
                    onClick = {
                        onSortSelected(index + 1)
                        expanded = false
                    }
                )
            }
        }
    }
}
