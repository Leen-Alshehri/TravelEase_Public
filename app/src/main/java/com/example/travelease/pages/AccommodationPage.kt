package com.example.travelease.pages
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.example.travelease.firebaseDB.entities.Trip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.accommodationsApi.Property
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.alefFontFamily
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Accommodation
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationScreen(
    viewModel: AccommodationViewModel,
    navController: NavController,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel,
    mode: String
)

 {

    val accommodations by viewModel.accommodations
    val errorMessage by viewModel.errorMessage
    val sortBy = viewModel.sortBy
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid

    Log.d("AccommodationScreen", "Received IDs: Traveler ID: $travelerId, Trip ID: $tripId, Itinerary ID: $itineraryId")

    if (travelerId == "INVALID_ID" || tripId == "INVALID_ID" || itineraryId == "INVALID_ID") {
        Log.e("AccommodationScreen", "ERROR: Invalid Navigation IDs!")
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Available Accommodations", fontFamily = alefFontFamily)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        //navController.navigate(Screens.AccommodationsSearch.route)
                        //{
                           // popUpTo(navController.graph.findStartDestination().id) }
                    }) {
                        androidx.compose.material.Icon(
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
                    text="Accommodations in ${viewModel.destination.value}",
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
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
            } else {
                LazyColumn {
                    items(accommodations) { property ->
                        if (mode == "specificTrip") {
                            AccommodationItem(
                                property = property,
                                travelerId = travelerId ?: "INVALID_ID",
                                tripId = tripId ?: "INVALID_ID",
                                itineraryId = itineraryId ?: "INVALID_ID",
                                dbViewModel = dbViewModel,
                                checkInDate = viewModel.checkInDate.value,
                                checkOutDate = viewModel.checkOutDate.value
                            )
                        } else {
                            AccommodationItemWithTripPicker(
                                property = property,
                                travelerId = travelerId ?: "INVALID_ID",
                                dbViewModel = dbViewModel,
                                checkInDate = viewModel.checkInDate.value,
                                checkOutDate = viewModel.checkOutDate.value
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccommodationItem(
    property: Property,
    travelerId: String,
    tripId: String,
    itineraryId: String,
    dbViewModel: dbViewModel,
    checkInDate: String,
    checkOutDate: String
) {
    val context = LocalContext.current// Access the current context
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Hotel Image
            val imageUrl = property.images?.firstOrNull()?.original_image ?: ""
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = property.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop //fills the bounds uniformly
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Hotel name
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(property.link))
                            context.startActivity(intent)
                        }
                    )


                    // Rating + Reviews
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐ ${property.overall_rating ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "(${property.reviews ?: 0} reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    val nightPrice = property.rate_per_night?.extracted_lowest ?: 0f
                    Text(
                        text = if (nightPrice == 0f) "SAR N/A / night" else "SAR ${String.format("%.2f", nightPrice)} / night",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (property.eco_certified) {
                        Text(
                            text = "🌱 Eco-Friendly",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "View Details",
                        color = Color(0xFF0C3D8D),
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable { showDetails = true }
                            .padding(top = 4.dp)
                    )
                    if (showDetails) {
                        AccommodationDetails(property = property, onDismiss = { showDetails = false })
                    }

                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Accommodation")
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                if (!showSuccessAnimation) {
                    Text(
                        text = "Add Accommodation?",
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
                            modifier = Modifier.size(250.dp), // Success animation size
                            animationFile = "success_check.json"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Success message
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
                            text = "Are you sure you want to add this accommodation to your trip?",
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
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
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

                                val accommodationData = Accommodation(
                                    name = property.name,
                                    itineraryId = "",
                                    checkIn = checkInDate,
                                    checkOut = checkOutDate,
                                    rating = property.overall_rating,
                                    location = property.address,
                                    description = property.description,
                                    hotelClass = property.hotel_class,
                                    accommodationId = UUID.randomUUID().toString(),
                                    reviews = property.reviews,
                                    pricePerNight = "SAR ${String.format("%.2f", (property.rate_per_night?.extracted_lowest ?: 0f))} / night"
                                )


                                dbViewModel.addAccommodationToItinerary(
                                    travelerId,
                                    tripId = tripId,
                                    itineraryId = itineraryId,
                                    accommodation = accommodationData,
                                    onSuccess = { Log.d("Firestore", "Accommodation added successfully!") },
                                    onFailure = { e -> Log.e("Firestore", "Error: ${e.message}") }
                                )

                                Log.d("Firestore", "Accommodation added successfully for traveler $travelerId")

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(2500)
                                    showDialog = false
                                    showSuccessAnimation = false
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
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
@Composable
fun AccommodationItemWithTripPicker(
    property: Property,
    travelerId: String,
    dbViewModel: dbViewModel,
    checkInDate: String,
    checkOutDate: String
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDetails by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val selectedTripState = remember { mutableStateOf<Trip?>(null) }
    val selectedTrip = selectedTripState.value

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
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                val imageUrl = property.images?.firstOrNull()?.original_image ?: ""
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = property.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val context = LocalContext.current
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            ),
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(property.link))
                                context.startActivity(intent)
                            }
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐ ${property.overall_rating ?: "N/A"}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "(${property.reviews ?: 0} reviews)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        val nightPrice = property.rate_per_night?.extracted_lowest ?: 0f
                        Text(
                            text = if (nightPrice == 0f) "SAR N/A / night" else "SAR ${String.format("%.2f", nightPrice)} / night",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (property.eco_certified) {
                            Text(
                                text = "🌱 Eco-Friendly",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "View Details",
                            color = Color(0xFF0C3D8D),
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { showDetails = true }
                                .padding(top = 4.dp)
                        )
                    }
                    if (showDetails) {
                        AccommodationDetails(property = property, onDismiss = { showDetails = false })
                    }


                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add to trip")
                        }

                        DropdownMenu(expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(
                                text = {Text("Choose a trip", fontWeight = FontWeight.Bold)},
                                onClick = {}
                            )
                            (currentTrips + upcomingTrips).forEach { trip ->
                                DropdownMenuItem(
                                    text = { Text(trip.name) },
                                    onClick = {
                                        selectedTripState.value = trip
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDialog && selectedTrip != null) {
        val trip = selectedTrip!!// none nullable

        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                if (!showSuccessAnimation) {
                    Text(
                        text = "Add Accommodation?",
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
                            text = "Add this accommodation to '${trip.name}'?",
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
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
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

                                val accommodationData = Accommodation(
                                    name = property.name,
                                    itineraryId = trip.itineraryId,
                                    checkIn = checkInDate,
                                    checkOut = checkOutDate,
                                    rating = property.overall_rating,
                                    location = property.address,
                                    description = property.description,
                                    hotelClass = property.hotel_class,
                                    accommodationId = UUID.randomUUID().toString(),
                                    pricePerNight = "SAR ${String.format("%.2f", (property.rate_per_night?.extracted_lowest ?: 0f))} / night"
                                )

                                dbViewModel.addAccommodationToItinerary(
                                    travelerId = travelerId,
                                    tripId = trip.tripId,
                                    itineraryId = trip.itineraryId,
                                    accommodation = accommodationData,
                                    onSuccess = { Log.d("Firestore", "Accommodation added to ${trip.name}") },
                                    onFailure = { e -> Log.e("Firestore", "Error: ${e.message}") }
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
                            Text("Add", color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AccommodationDetails(property: Property, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = property.name ?: "No Name",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                TitleSection(title = "📍 Address", content = property.address?.takeIf { it.isNotEmpty() } ?: "N/A")
                TitleSection(title = "🏨 Class", content = property.hotel_class ?: "N/A")
                TitleSection(title = "⭐ Rating", content = property.overall_rating?.toString() ?: "N/A")
                TitleSection(title = "💬 Reviews", content = property.reviews?.toString() ?: "N/A")
                TitleSection(
                    title = "🕐 Check-in / Check-out",
                    content = "${property.check_in_time?.takeIf { it.isNotEmpty() } ?: "N/A"} | ${property.check_out_time?.takeIf { it.isNotEmpty() } ?: "N/A"}"
                )
                TitleSection(
                    title = "📝 Description",
                    content = property.description?.takeIf { it.isNotEmpty() } ?: "No description available."
                )

                property.amenities?.takeIf { it.isNotEmpty() }?.let {
                    DetailListSection("✅ Amenities", it)
                }

                property.essential_info?.takeIf { it.isNotEmpty() }?.let {
                    DetailListSection("📌 Essential Info", it)
                }

                property.prices?.takeIf { it.isNotEmpty() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💲 Price Sources", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    it.forEach { price ->
                        val source = price.source.takeIf { !it.isNullOrEmpty() } ?: "Unknown"
                        val rate = price.rate_per_night?.lowest?.takeIf { it != "0" && it != "0.0" } ?: "N/A"
                        Text("- $source: $rate", fontSize = 14.sp)
                    }
                }

                property.nearby_places?.takeIf { it.isNotEmpty() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📍 Nearby Places", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    it.forEach { place ->
                        val transportInfo = place.transportations
                            ?.takeIf { it.isNotEmpty() }
                            ?.joinToString { t -> "${t.type} (${t.duration})" }
                            ?: "No transport info"
                        Text("- ${place.name}: $transportInfo", fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


@Composable
fun TitleSection(title: String, content: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    Text(content, fontSize = 14.sp)
}

@Composable
fun DetailListSection(title: String, items: List<String>) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(4.dp))
    items.forEach { item ->
        Text("- $item", fontSize = 14.sp)
    }
}

