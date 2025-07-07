package com.example.travelease.pages

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.travelease.activityApi.ActivitiesViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Activity
import com.example.travelease.firebaseDB.entities.Trip
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.alefFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivitiesViewModel,
    navController: NavController,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel
) {
    val activities by viewModel.activities
    val errorMessage by viewModel.errorMessage
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    val tripList =
        dbViewModel.currentTrips.collectAsState().value + dbViewModel.upcomingTrips.collectAsState().value
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var showTripDropdown by remember { mutableStateOf(false) }
    val trips =
        dbViewModel.currentTrips.collectAsState().value + dbViewModel.upcomingTrips.collectAsState().value


    Log.d(
        "ActivityScreen",
        "Received IDs: Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId"
    )

    if (travelerId == "INVALID_ID" || tripId == "INVALID_ID" || itineraryId == "INVALID_ID") {
        Log.e("ActivityScreen", "ERROR: Invalid Navigation IDs!")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Available Activities", fontFamily = alefFontFamily)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { values ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(values)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            }

            if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 8.dp, end = 8.dp)
            ) {
                items(activities) { activity ->
                    var showTripDropdown by remember { mutableStateOf(false) }
                    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
                    var showDialog by remember { mutableStateOf(false) }
                    var showSuccessAnimation by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activity.date.eventTime,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activity.address.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                activity.venue?.let { venue ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rating",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${venue.rating} (${venue.reviews} reviews)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    if (tripId == "" || itineraryId == "") {
                                        showTripDropdown = true
                                    } else {
                                        showDialog = true
                                    }

                                },
                                    modifier = Modifier.align(alignment = Alignment.BottomEnd)) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Activity",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (showTripDropdown) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { showTripDropdown = false },
                                    containerColor = Color.White,
                                    modifier = Modifier.background(Color.White),
                                    offset = DpOffset(x = 250.dp, y = 0.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = {Text("Choose a trip", fontWeight = FontWeight.Bold)},
                                        onClick = {}
                                    )
                                    trips.forEach { trip ->
                                        DropdownMenuItem(
                                            text = { Text(trip.name) },
                                            onClick = {
                                                selectedTrip = trip
                                                showTripDropdown = false
                                                showDialog = true
                                            }
                                        )
                                    }
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
                                        text = "Add Activity?",
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
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
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
                                            text = "Are you sure you want to add this activity to your trip?",
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
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color.Gray
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text("Cancel")
                                        }


                                        Button(
                                            onClick = {
                                                showSuccessAnimation = true

                                                if (tripId == "" || itineraryId == ""){
                                                    val activityData = Activity(
                                                        name = activity.title,
                                                        sdate = activity.date.startDate,
                                                        location = activity.address.joinToString(", "),
                                                        link = activity.link,
                                                        activityId = UUID.randomUUID().toString(),
                                                        rating = activity.venue?.rating ?: 0.0,
                                                        description = activity.description ?: "",
                                                        itineraryId = selectedTrip?.itineraryId ?: "",
                                                        reviews = activity.venue?.reviews
                                                    )

                                                    dbViewModel.addActivityToItinerary(
                                                        travelerId,
                                                        itineraryId = selectedTrip?.itineraryId,
                                                        activity = activityData,
                                                        onSuccess = { Log.d("Firestore",
                                                            "Activity added successfully!") },
                                                        onFailure = { e: Exception -> Log.e(
                                                            "Firestore", "Error: ${e.message}") },
                                                        tripId = selectedTrip?.tripId ?: ""
                                                    )
                                                }
                                                else{
                                                    val activityData = Activity(
                                                        name = activity.title,
                                                        sdate = activity.date.startDate,
                                                        location = activity.address.joinToString(", "),
                                                        link = activity.link,
                                                        activityId = UUID.randomUUID().toString(),
                                                        rating = activity.venue?.rating ?: 0.0,
                                                        description = activity.description ?: "",
                                                        itineraryId = itineraryId ?: "",
                                                        reviews = activity.venue?.reviews
                                                    )

                                                    dbViewModel.addActivityToItinerary(
                                                        travelerId,
                                                        itineraryId = itineraryId,
                                                        activity = activityData,
                                                        onSuccess = { Log.d("Firestore",
                                                            "Activity added successfully!") },
                                                        onFailure = { e: Exception -> Log.e(
                                                            "Firestore", "Error: ${e.message}") },
                                                        tripId = tripId
                                                    )
                                                }

                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(2500)
                                                    showDialog = false
                                                    showSuccessAnimation = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFF0C3D8D
                                                )
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFDADCE0))
                                        ) {
                                            Text("Add", color = Color.White)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


