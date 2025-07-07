package com.example.travelease.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import com.example.travelease.firebaseDB.entities.Flight
import com.example.travelease.firebaseDB.entities.Accommodation
import com.example.travelease.firebaseDB.entities.Activity
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Expense
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryPage(navController: NavController, tripId: String, itineraryId: String, tripName: String, dbViewModel: dbViewModel) {
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    val isPrevious = remember { mutableStateOf(false) }
    LaunchedEffect(tripId) {
        isPrevious.value = dbViewModel.isPreviousTripById(tripId)
    }


    val flights = remember { mutableStateListOf<Flight>() }
    val accommodations = remember { mutableStateListOf<Accommodation>() }
    val activities = remember { mutableStateListOf<Activity>() }

    if (travelerId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: Traveler not found!", color = Color.Red, fontSize = 18.sp)
        }
        return
    }





    // Fetch data using dbViewModel
    LaunchedEffect(tripId, itineraryId) {
        dbViewModel.getFlightsByItinerary(travelerId, tripId, itineraryId) { fetchedFlights ->
            flights.clear()
            flights.addAll(fetchedFlights)
        }
        dbViewModel.getAccommodationsByItinerary(travelerId, tripId, itineraryId) { fetchedAccommodations ->
            accommodations.clear()
            accommodations.addAll(fetchedAccommodations)
        }
        dbViewModel.getActivitiesByItinerary(travelerId, tripId, itineraryId) { fetchedActivities ->
            activities.clear()
            activities.addAll(fetchedActivities)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = tripName, fontFamily = alefFontFamily) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
            )
        },
        containerColor = Color.White
    ) { values ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
                .background(color = Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { item{
            ItineraryTabs(selectedTabIndex, navController, dbViewModel, tripId, itineraryId,isPrevious.value)
        }}
    }
}

// Tabs Section
@Composable
fun ItineraryTabs(
    selectedTabIndex: MutableState<Int>,
    navController: NavController,
    dbViewModel: dbViewModel,
    tripId: String,
    itineraryId: String,
    isPrevious: Boolean
) {
    val tabTitles = listOf("Trip Summary", "Expenses")

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
                        fontSize = 14.sp,
                        color = if (selectedTabIndex.value == index) Orange else Color.Gray
                    )
                }
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    when (selectedTabIndex.value) {
        0 -> TripSummarySection(navController, dbViewModel, tripId, itineraryId, isPrevious)
        1 -> ExpensesSection(navController, dbViewModel, tripId, itineraryId)
    }
}

@Composable
fun TripSummarySection(
    navController: NavController, dbViewModel: dbViewModel, tripId: String, itineraryId: String,
    isPrevious: Boolean
) {
    Column(modifier = Modifier.padding(16.dp)) {
        FlightSection(navController, tripId, itineraryId,dbViewModel, isPrevious)
        AccommodationSection(navController,tripId,itineraryId,dbViewModel, isPrevious)
        ActivitySection(navController,tripId,itineraryId,dbViewModel, isPrevious)
    }
}


@Composable
fun FlightSection(navController: NavController, tripId: String, itineraryId: String, dbViewModel: dbViewModel, isPrevious: Boolean) {
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    val flights = remember { mutableStateListOf<Flight>() }

    if (travelerId == null) {
        return
    }

    // Fetch flights when the page loads
    //LaunchedEffect(tripId, itineraryId) {
        //dbViewModel.getFlightsByItinerary(travelerId, tripId, itineraryId) { fetchedFlights ->
         //   flights.clear()
            //flights.addAll(fetchedFlights)
      //  }
   // }
      // Function to refresh the flights
      fun refreshFlights() {
          dbViewModel.getFlightsByItinerary(travelerId, tripId, itineraryId) { fetchedFlights ->
              flights.clear()
              flights.addAll(fetchedFlights)
          }
      }

    // Initial load
    LaunchedEffect(tripId, itineraryId) {
        refreshFlights()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Flights", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        //flights.forEach { flight ->
          //  FlightItem(flight)
        //}
        flights.forEach { flight ->
            FlightItem(
                flight = flight,
                travelerId = travelerId,
                tripId = tripId,
                itineraryId = itineraryId,
                dbViewModel = dbViewModel,
                onFlightDeleted = { deletedFlight ->
                    flights.remove(deletedFlight)
                }
            )
        }

        // Hide button when 2 flights exist
        if (!isPrevious && flights.size < 2) {
            AddItemButton(
                text = "Add Flight",
                onClick = {
                    navController.navigate("flights_search?tabIndex=0&tripId=$tripId&itineraryId=$itineraryId")
                }
            )
        }
    }
}


@Composable
fun FlightItem(
    flight: Flight,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel,
    onFlightDeleted: (Flight) -> Unit,
    showDeleteIcon: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)) // Light Grey Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Route & badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${flight.from} → ${flight.to}",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
                Text(
                    text= flight.flightNo,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
                Text(
                    text = flight.direct,
                    fontWeight = FontWeight.Bold,
                    color = if (flight.direct.equals("Direct", ignoreCase = true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error

                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Flight segments
            flight.segments?.forEachIndexed { index, segment ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${segment.departure_airport.time.substring(11)} - ${segment.arrival_airport.time.substring(11)}"
                    )
                    Text(segment.airline)
                }

                // layover display
                if (index < (flight.segments.size - 1)) {
                    val layover = flight.layovers?.getOrNull(index)
                    layover?.let {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Text(
                                text = "Layover at ${it.name} (${it.id}) - ${it.duration} min",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            if (it.overnight) {
                                Text(
                                    text = "Overnight layover",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Price & delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${flight.price /3.75} SAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                if (showDeleteIcon) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete Flight")
                    }
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
                        text = "Delete Flight?",
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
                        Text("Deleted Successfully!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Are you sure you want to delete this flight?",
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
                                onFlightDeleted(flight)

                                dbViewModel.deleteFlightFromItinerary(
                                    travelerId = travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    flightId = flight.flightId,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            delay(2500)
                                            showDialog = false
                                            showSuccessAnimation = false
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e("Firestore", "Failed to delete: ${e.message}")
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}



@Composable
fun AccommodationSection(
    navController: NavController, tripId: String, itineraryId: String, dbViewModel: dbViewModel,
    isPrevious: Boolean
) {
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    val accommodations = remember { mutableStateListOf<Accommodation>() }

    if (travelerId == null) {
        return
    }

    // Fetch accommodations when the page loads
    LaunchedEffect(tripId, itineraryId) {
        dbViewModel.getAccommodationsByItinerary(travelerId, tripId, itineraryId) { fetchedAccommodations ->
            accommodations.clear()
            accommodations.addAll(fetchedAccommodations)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Accommodations", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Show accommodations if available
        if (accommodations.isEmpty()) {
            //Text("No accommodations added yet.", fontSize = 14.sp, color = Color.Gray)
        } else {
            accommodations.forEach { accommodation ->
                AccommodationItem(
                    accommodation = accommodation,
                    travelerId = travelerId,
                    tripId = tripId,
                    itineraryId = itineraryId,
                    dbViewModel = dbViewModel,
                    onAccommodationDeleted = { deleted ->
                        accommodations.remove(deleted)
                    }
                )
            }

        }
        if (!isPrevious) {
            AddItemButton("Add Accommodation") {
                navController.navigate("accommodations_search?tabIndex=1&tripId=$tripId&itineraryId=$itineraryId&mode=specificTrip")
            }
        }

    }
}



@Composable
fun AccommodationItem(
    accommodation: Accommodation,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel,
    onAccommodationDeleted: (Accommodation) -> Unit,
    showDeleteIcon: Boolean = true,
    showCheckInOutFlag: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accommodation.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (showCheckInOutFlag && accommodation.description != null) {
                    Text(
                        text = accommodation.description,
                        color = if (accommodation.description.equals("Check-In", ignoreCase = true)) OceanBlue else Orange,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }


                if (showDeleteIcon) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Delete Accommodation")
                    }
                }

            }

            Text(text = "Location: ${accommodation.location ?: "N/A"}", color = Color.Gray)
            Text(text = accommodation.pricePerNight ?: "")
            Text(text = "Rating: ${accommodation.rating ?: "N/A"} ⭐")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${accommodation.checkIn} - ${accommodation.checkOut}")
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
                        text = "Delete Accommodation?",
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
                        Text("Deleted Successfully!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Are you sure you want to delete this accommodation?",
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
                                onAccommodationDeleted(accommodation)

                                dbViewModel.deleteAccommodationFromItinerary(
                                    travelerId = travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    accommodationId = accommodation.accommodationId,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            delay(2500)
                                            showDialog = false
                                            showSuccessAnimation = false
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e("Firestore", "Failed to delete accommodation: ${e.message}")
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}




@Composable
fun ActivitySection(
    navController: NavController, tripId: String, itineraryId: String, dbViewModel: dbViewModel,
    isPrevious: Boolean
) {
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    val activities = remember { mutableStateListOf<Activity>() }

    if (travelerId == null) {
        return
    }

    // Fetch activities when the page loads
    LaunchedEffect(tripId, itineraryId) {
        dbViewModel.getActivitiesByItinerary(travelerId, tripId, itineraryId) { fetchedActivities ->
            activities.clear()
            activities.addAll(fetchedActivities)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Activities", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        // Show activities if available
        if (activities.isEmpty()) {
            //Text("No activities added yet.", fontSize = 14.sp, color = Color.Gray)
        } else {
            activities.forEach { activity ->
                ActivityItem(
                    activity = activity,
                    travelerId = travelerId,
                    tripId = tripId,
                    itineraryId = itineraryId,
                    dbViewModel = dbViewModel,
                    onActivityDeleted = { deletedActivity ->
                        activities.remove(deletedActivity)
                    }
                )
            }

        }
        if (!isPrevious) {
            AddItemButton("Add Activity") {
                navController.navigate("activities_search?tabIndex=2&tripId=$tripId&itineraryId=$itineraryId")
            }
        }
    }
}



@Composable
fun ActivityItem(
    activity: Activity, travelerId: String, tripId: String, itineraryId: String, dbViewModel: dbViewModel, onActivityDeleted: (Activity) -> Unit
    , showDeleteIcon: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showDeleteIcon) {
                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete Activity")
                    }
                }
            }

            Text(
                text = "Location: ${if (activity.location.isNullOrBlank()) "N/A" else activity.location}",
                color = Color.Gray,
                fontSize = 13.sp
            )
            Text(text = "Date: ${activity.sdate}")
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
                        text = "Delete Activity?",
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
                        Text("Deleted Successfully!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Are you sure you want to delete this activity?",
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
                                onActivityDeleted(activity)

                                dbViewModel.deleteActivityFromItinerary(
                                    travelerId = travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    activityId = activity.activityId,
                                    onSuccess = {
                                        coroutineScope.launch {
                                            delay(2500)
                                            showDialog = false
                                            showSuccessAnimation = false
                                        }
                                    },
                                    onFailure = { e ->
                                        Log.e("Firestore", "Failed to delete activity: ${e.message}")
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
        )
    }
}


@Composable
fun AddItemButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)), // Light grey
        border = null, // Remove border
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp, color = Color.Black)
    }
}


@Composable
fun ExpensesSection(
    navController: NavController,
    dbViewModel: dbViewModel,
    tripId: String,
    itineraryId: String,
) {
    val context = LocalContext.current
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var budget by remember { mutableStateOf(0f) }
    var totalExpenses by remember { mutableStateOf(0f) }
    var netBalance by remember { mutableStateOf(0f) }
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (travelerId == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(itineraryId) {
        dbViewModel.getBudget(travelerId, tripId, itineraryId) {
            budget = it.budget.toFloat() ?: 0f
            totalExpenses = it.totalExpenses.toFloat() ?: 0f
            netBalance = it.netBalance.toFloat() ?: 0f
        }
        dbViewModel.getExpenses(travelerId, tripId, itineraryId) {
            expenses = it
        }
    }





            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Net Balance", style = MaterialTheme.typography.labelMedium, color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
                Text(
                    text = "SAR ${"%,.2f".format(netBalance)}",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(5.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("↓ Budget", color = Color(0xFF0C3D8D), fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                        Text("SAR ${"%,.2f".format(budget)}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFFF0F0F0), CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Edit Budget", tint = Color.LightGray)
                        }
                        Text("Edit", color = Color.Gray, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("↑ Expenses", color = Color(0xFF0C3D8D), fontWeight = FontWeight.SemiBold,fontSize = 20.sp)
                        Text("-SAR ${"%,.2f".format(totalExpenses)}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFFF0F0F0), CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.LightGray)
                        }
                        Text("Add", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("History", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                //Gray Line
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(5.dp))


                // Scrollable expense list with limited height
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 250.dp)
                ) {
                    items(expenses.sortedByDescending { it.amount }) { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(expense.name, fontWeight = FontWeight.Bold)

                            Row {
                                val (text, color) = if (expense.name.lowercase().contains("budget")) {
                                    "+ SAR ${"%,.2f".format(expense.amount)}" to Color(0xFF00A651)
                                } else {
                                    "- SAR ${"%,.2f".format(expense.amount)}" to Color(0xFFD32F2F)
                                }

                                Text(text = text, color = color, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            dbViewModel.deleteExpense(travelerId, tripId, itineraryId, expense.expenseId ?: "") {
                                                dbViewModel.getExpenses(travelerId, tripId, itineraryId) { expenses = it }
                                                dbViewModel.getBudget(travelerId, tripId, itineraryId) {
                                                    budget = it.budget.toFloat() ?: 0f
                                                    totalExpenses = it.totalExpenses.toFloat() ?: 0f
                                                    netBalance = it.netBalance.toFloat() ?: 0f
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

    if (showAddDialog) {
        var expenseName by remember { mutableStateOf("") }
        var expenseAmount by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = { Text("Add Expense") },
            text = {
                Column {
                    OutlinedTextField(
                        value = expenseName,
                        onValueChange = { expenseName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        isError = errorMessage.isNotBlank() && expenseName.isBlank()
                    )
                    if (errorMessage.isNotBlank() && expenseName.isBlank()) {
                        Text(text = "Expense name cannot be empty! Please enter a valid name.", color = Color.Red, fontSize = 12.sp)
                    }
                    OutlinedTextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMessage.isNotBlank() && (expenseAmount.toFloatOrNull() ?: 0f) <= 0f
                    )
                    if (errorMessage.isNotBlank() && (expenseAmount.toFloatOrNull() ?: 0f) <= 0f) {
                        Text(text = "Amount cannot be negative or zero!, Please enter a valid number to proceed.", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = expenseAmount.toFloatOrNull() ?: 0f
                    if (expenseName.isBlank()) {
                        errorMessage = "Expense name cannot be empty!, Please enter valid name."
                    } else if (amount <= 0f) {
                        errorMessage = "Amount cannot be negative or zero!, Please enter valid number to proceed."
                    } else {
                    //if (expenseName.isNotBlank() && amount > 0f) {
                        dbViewModel.addExpense(travelerId, tripId, itineraryId, expenseName, amount) {
                            showAddDialog = false
                            dbViewModel.getExpenses(travelerId, tripId, itineraryId) { expenses = it }
                            dbViewModel.getBudget(travelerId, tripId, itineraryId) {
                                budget = it.budget.toFloat() ?: 0f
                                totalExpenses = it.totalExpenses.toFloat() ?: 0f
                                netBalance = it.netBalance.toFloat() ?: 0f
                            }
                        }
                    }
                },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D))
                ) {
                    Text(text="Add",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D848D))
                ) {
                    Text(text="Cancel",
                        fontSize = 16.sp,
                        color = Color.White)
                }
            }
        )
    }

    if (showEditDialog) {
        var newBudgetAmount by remember { mutableStateOf(budget.toString()) }
        var errorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = { Text("Edit Budget") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newBudgetAmount,
                        onValueChange = {
                            newBudgetAmount = it
                            if ((it.toFloatOrNull() ?: 0f) < 0f) {
                                errorMessage = "Amount cannot be negative!, Please enter valid number to proceed."
                            } else {
                                errorMessage = ""
                            }
                        },
                        label = { Text("Budget Amount") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = errorMessage.isNotBlank()
                    )
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = newBudgetAmount.toFloatOrNull()
                    if (amount != null && amount >= 0f) {
                        if (budget > 0f) {
                            // Update existing budget
                            dbViewModel.updateBudgetAmount(travelerId, tripId, itineraryId, amount) {
                                dbViewModel.addExpense(
                                    travelerId = travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    name = "Budget Updated",
                                    amount = amount
                                ) {
                                    showEditDialog = false
                                    dbViewModel.getExpenses(travelerId, tripId, itineraryId) { expenses = it }
                                    dbViewModel.getBudget(travelerId, tripId, itineraryId) {
                                        budget = it.budget.toFloat() ?: 0f
                                        totalExpenses = it.totalExpenses.toFloat() ?: 0f
                                        netBalance = it.netBalance.toFloat() ?: 0f
                                    }
                                }
                            }
                        } else {
                            // Add new budget
                            dbViewModel.addBudget(travelerId, tripId, itineraryId, amount) {
                                //"Initial Budget" in expenses history
                                dbViewModel.addExpense(
                                    travelerId = travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    name = "Initial Budget",
                                    amount = amount
                                ) {
                                    showEditDialog = false
                                    dbViewModel.getExpenses(travelerId, tripId, itineraryId) { expenses = it }
                                    dbViewModel.getBudget(travelerId, tripId, itineraryId) {
                                        budget = it.budget.toFloat() ?: 0f
                                        totalExpenses = it.totalExpenses.toFloat() ?: 0f
                                        netBalance = it.netBalance.toFloat() ?: 0f
                                    }
                                }
                            }
                        }
                    }
                    else {
                        errorMessage = "Amount cannot be negative!, Please enter valid number to proceed."
                    }
                },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                ) {
                    Text(text="Save",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7D848D))
                ) {
                    Text(text = "Cancel", fontSize = 16.sp, color = Color.White)
                }
            }
        )
    }
}



