package com.example.travelease.pages

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.FlightSegment
import com.example.travelease.recommenderSystem.accommodation
import com.example.travelease.firebaseDB.entities.Accommodation
import com.example.travelease.firebaseDB.entities.Activity
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.flightsApi.BestFlight
import com.example.travelease.firebaseDB.entities.Flight
import com.example.travelease.recommenderSystem.activity
import com.example.travelease.flightsApi.Layover
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationPage(
    viewModel: RecommendationViewModel,
    navController: NavController,
    dbViewModel: dbViewModel
) {
    val activities by viewModel.activity
    val accommodation by viewModel.accommodation
    val destination by viewModel.destination
    val errorMessage by viewModel.errorMessage
    val flights by viewModel.flights
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var addAllEnabled by remember { mutableStateOf(false) }
    var flightData: Flight = Flight()
    var accommodationData: Accommodation = Accommodation()
    var activityData: Activity = Activity()
    var addAllCount by remember { mutableIntStateOf(0) }


    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("MMM dd")
        return format.format(date)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Recommended Trip", fontFamily = alefFontFamily)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
            )
        }
    ) { values ->
        if (viewModel.isLoading.value){
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)){
                CircularProgressIndicator()
            }
        }
        else if (errorMessage.isNotEmpty()) {
            Text(
                text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(values)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (accommodation != null) {
                        Text(
                            text = convertLongToTime(convertDateToLong(accommodation!!.checkin)) +
                                    " - " + convertLongToTime(convertDateToLong(accommodation!!.checkout)),
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    else
                        Text( text = "" )
                }
                HorizontalDivider(thickness = 2.dp, color = Orange)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (destination != "")
                        Text(
                            text = "We have planned you a Personalized trip \n to ${destination ?: ""}!",
                            fontSize = 16.sp,
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue,
                            style = TextStyle(textAlign = TextAlign.Center)
                        )
                    else
                        Text( text = "" )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Button(
                            onClick = {
                                if (addAllCount < 1){
                                    showDialog = true
                                    addAllEnabled = true
                                }

                            },
                            modifier = Modifier.size(width = 110.dp, height = 33.dp),
                            shape = RoundedCornerShape(12),
                            colors = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) {
                            Text(
                                text = "Add All",
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = Color.White,
                            title = {
                                if (!showSuccessAnimation) {
                                    Text(
                                        text = "Add Trip?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Orange,
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
                                            text = "Are you sure you want to add this trip?",
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

                                                if (flights.isNotEmpty()) {
                                                    flightData =
                                                        Flight(
                                                            flightNo = flights.first().flights
                                                                .first().flight_number,
                                                            itineraryId = "",
                                                            from = flights.first().flights.first()
                                                                .departure_airport.id,
                                                            to = flights.first().flights.last()
                                                                .arrival_airport.id,
                                                            departureTime = flights.first()
                                                                .flights.first().departure_airport.time,
                                                            arrivalTime = flights.first()
                                                                .flights.last().arrival_airport.time,
                                                            flightId = UUID.randomUUID().toString(),
                                                            price = flights.first().price * 3.75,
                                                            direct = if (flights.first().isDirect)
                                                                "Direct" else "Non-Direct",
                                                            airline = flights.first()
                                                                .flights.first().airline,
                                                            duration = flights.first()
                                                                .flights.first().duration,
                                                            layovers = flights.first()
                                                                .layovers,
                                                            segments = flights.first()
                                                                .flights.map {
                                                                FlightSegment(
                                                                    departure_airport = it.departure_airport,
                                                                    arrival_airport = it.arrival_airport,
                                                                    duration = it.duration,
                                                                    airline = it.airline,
                                                                    flight_number = it.flight_number
                                                                )
                                                            },
                                                            flightDate = flights.first().
                                                            flights.first().departure_airport.time.
                                                            substring(
                                                                0,
                                                                10
                                                            )
                                                        )
                                                }
                                                if (accommodation != null) {
                                                    accommodationData = Accommodation(
                                                        name = accommodation?.name ?: "",
                                                        itineraryId = "",
                                                        checkIn = viewModel.startDate.value,
                                                        checkOut = viewModel.endDate.value,
                                                        rating = accommodation?.rating ?: 0.0f,
                                                        location = null,
                                                        description = accommodation?.description,
                                                        hotelClass = null,
                                                        accommodationId = UUID.randomUUID()
                                                            .toString(),
                                                        reviews = accommodation?.reviews ?: 0,
                                                        pricePerNight = "SAR ${
                                                            String.format(
                                                                "%.2f",
                                                                (accommodation?.price ?: 0f) * 3.75
                                                            )
                                                        } / night"
                                                    )
                                                }

                                                if (travelerId != null)  {
                                                    if (flights.isNotEmpty() ||
                                                        accommodation != null ||
                                                        activities.isNotEmpty()) {
                                                        addAllCount = 1
                                                        dbViewModel.getTripBuNameForAddAll(
                                                            travelerId = travelerId,
                                                            tripName = viewModel.tripName.value,
                                                            startDate = viewModel.startDate.value,
                                                            endDate = viewModel.endDate.value,
                                                            imageUri = viewModel.imageUri.value,
                                                            accommodation = accommodationData,
                                                            flight = flightData,
                                                            activities = activities,
                                                            onSuccess = {
                                                                Log.d(
                                                                    "Firestore",
                                                                    "add all functionality done " +
                                                                            "successfully for traveler" +
                                                                            " $travelerId"
                                                                )
                                                            }
                                                        )
                                                    }
                                                }

                                                coroutineScope.launch {
                                                    kotlinx.coroutines.delay(2500)
                                                    showDialog = false
                                                    showSuccessAnimation = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = OceanBlue
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, start = 8.dp, end = 8.dp)
                ) {
                    item {
                        Text(
                            text = "Recommended Flight",
                            fontSize = 20.sp,
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (flights.isNotEmpty()) {
                            if (travelerId != null) {
                                FlightInfo(flights.first(), travelerId, viewModel,
                                    dbViewModel::getTripByNameForFlights)
                            }
                        }
                         else {
                            Text(
                                text = "no flights found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Text(
                            text = "Recommended Accommodation",
                            fontSize = 20.sp,
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (accommodation != null) {
                            accommodation?.let {
                                if (travelerId != null) {
                                    AccommodationInfo(accommodation = it,
                                        travelerId,
                                        viewModel,
                                        dbViewModel::getTripByNameForAccommodations)
                                }
                             }
                        }
                        else{
                            Text(
                                text = "no accommodations found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Recommended Activities",
                            fontSize = 20.sp,
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                    if (activities.isNotEmpty()) {
                        items(activities) { activity ->
                            if (travelerId != null) {
                                ActivitiesInfo(activity, travelerId, viewModel,
                                    dbViewModel::getTripByNameForActivities)
                            }
                        }
                    }
                    else {
                        item {
                            Text(
                                text = "no activities found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccommodationInfo(accommodation: accommodation,
                      travelerId: String,
                      viewModel: RecommendationViewModel,
                      getTripByName: (tripName: String, travelerId: String, startDate: String,
                                      endDate: String, imageUri: String?,
                                      accommodation: Accommodation,
                                      onSuccess: () -> Unit) -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Hotel Image
            val imageUrl = accommodation.image ?: ""
            Image(
                painter = coil.compose.rememberAsyncImagePainter(imageUrl),
                contentDescription = accommodation.name,
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
                        text = accommodation.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable {
                            // Launch a browser intent with the property link
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(accommodation.link))
                            context.startActivity(intent)
                        }
                    )

                    // Rating + Reviews
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐ ${accommodation.rating ?: "N/A"}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "(${accommodation.reviews ?: 0} reviews)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "SAR ${String.format("%.2f", (accommodation.price ?: 0f))} / night",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                IconButton(onClick = {showDialog = true}) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Accommodation")
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
                        text = "Add Accommodation?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Orange,
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
                        androidx.compose.material.Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp
                        )
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

                                val accommodationData =
                                    Accommodation(
                                        name = accommodation.name,
                                        itineraryId = "",
                                        checkIn = viewModel.startDate.value,
                                        checkOut = viewModel.endDate.value,
                                        rating = accommodation.rating,
                                        location = null,
                                        description = accommodation.description,
                                        hotelClass = null,
                                        accommodationId = UUID.randomUUID().toString(),
                                        reviews = accommodation.reviews,
                                        pricePerNight = "SAR ${
                                            String.format(
                                                "%.2f",
                                                (accommodation.price ?: 0f) * 3.75
                                            )
                                        } / night"
                                    )


                                getTripByName(
                                    viewModel.tripName.value,
                                    travelerId,
                                    viewModel.startDate.value,
                                    viewModel.endDate.value,
                                    viewModel.imageUri.value,
                                    accommodationData)
                                {Log.d("Firestore", "Accommodation added successfully!")}

            Log.d("Firestore", "Accommodation added successfully for traveler $travelerId")

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(2500)
                                    showDialog = false
                                    showSuccessAnimation = false
                                }
                            },
                            colors = androidx.compose.material3.
                            ButtonDefaults.buttonColors(containerColor = OceanBlue),
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
fun FlightInfo(flight: BestFlight,
               travelerId: String,
               viewModel: RecommendationViewModel,
               getTripByName: (tripName: String, travelerId: String, startDate: String,
                               endDate: String, imageUri: String?, flight: Flight,
                               onSuccess: () -> Unit) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box {
        // Main Card UI
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

                /* flight.carbon_emissions?.difference_percent?.let { diff ->
                     val ecoText = when {
                         diff < -10 -> "🌿 Eco: ${-diff}% below avg"
                         diff > 10 -> "🔥 ${diff}% above avg"
                         else -> null
                     }
                     ecoText?.let {
                         Spacer(modifier = Modifier.height(4.dp))
                         Text(
                             text = it,
                             color = if (diff < -10) Color(0xFF388E3C) else Color(0xFFD32F2F),
                             fontSize = 13.sp
                         )
                     }
                 }*/

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${flight.price * 3.75} SAR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Flight")
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
                                text = "Add Flight?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Orange,
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

                                        val flightData =
                                            Flight(
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
                                        getTripByName(
                                            viewModel.tripName.value,
                                            travelerId,
                                            viewModel.startDate.value,
                                            viewModel.endDate.value,
                                            viewModel.imageUri.value,flightData )
                                        {Log.d("Firestore", "Flight added to " +
                                                viewModel.tripName.value
                                        )}

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
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OceanBlue
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


@Composable
fun Layover(layover: Layover) {
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
fun ActivitiesInfo(activity: activity,
                   travelerId: String,
                   viewModel: RecommendationViewModel,
                   getTripByName: (tripName: String, travelerId: String, startDate: String,
                                   endDate: String, imageUri: String?,
                                   activity: Activity, onSuccess: () -> Unit) -> Unit) {

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
                text = activity.date,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.rating} (${activity.reviews} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {showDialog = true}) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Activity",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                                val activityData = Activity(
                                    name = activity.title,
                                    itineraryId = "",
                                    sdate = activity.date,
                                    location = activity.address.joinToString(", "),
                                    link = "",
                                    activityId = UUID.randomUUID().toString(),
                                    rating = activity.rating,
                                    description = activity.description ?: "",
                                    reviews = activity.reviews
                                )

                                getTripByName(
                                    viewModel.tripName.value,
                                    travelerId,
                                    viewModel.startDate.value,
                                    viewModel.endDate.value,
                                    viewModel.imageUri.value,activityData) {
                                    Log.d(
                                        "Firestore",
                                        "Activity added successfully!"
                                    )
                                }

                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(2500)
                                    showDialog = false
                                    showSuccessAnimation = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OceanBlue
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


