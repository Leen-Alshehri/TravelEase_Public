package com.example.travelease.pages

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.R
import coil.compose.rememberAsyncImagePainter
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Trip
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.navigation.Screens
import kotlinx.coroutines.delay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: dbViewModel,
    recommendationViewModel: RecommendationViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var showSnackbarMessage by remember { mutableStateOf<String?>(null) }

    if (travelerId == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val currentTrips by viewModel.currentTrips.collectAsState()
    val previousTrips by viewModel.previousTrips.collectAsState()
    val upcomingTrips by viewModel.upcomingTrips.collectAsState()

    var showAllCurrent by remember { mutableStateOf(false) }
    var showAllUpcoming by remember { mutableStateOf(false) }
    var showAllPrevious by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(travelerId) {
        viewModel.fetchTrips(travelerId)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "Trips", fontFamily = alefFontFamily) },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                    ) {
                        Text(
                            text = "Create Trip",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

        ) { values ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(values)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*Current Trips */
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Current Trips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Orange,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentTrips.size > 1) {
                            Text(
                                text = if (showAllCurrent) "View less" else "View all",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                modifier = Modifier.clickable { showAllCurrent = !showAllCurrent }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (currentTrips.isNotEmpty()) {
                    item {
                        TripCard(
                            currentTrips.first(),
                            viewModel,
                            travelerId,
                            navController,
                            onDeleteSuccess = { showSnackbarMessage = "Trip deleted successfully!" })
                    }
                    if (showAllCurrent) {
                        items(currentTrips.drop(1)) { trip ->
                            TripCard(
                                trip,
                                viewModel,
                                travelerId,
                                navController,
                                onDeleteSuccess = {
                                    showSnackbarMessage = "Trip deleted successfully!"
                                })
                        }
                    }
                } else {
                    item { EmptyTripCard("No Current Trips!") }
                }

                /*Upcoming Trips*/
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Upcoming Trips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        if (upcomingTrips.size > 1) {
                            Text(
                                text = if (showAllUpcoming) "View less" else "View all",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                modifier = Modifier.clickable { showAllUpcoming = !showAllUpcoming }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (upcomingTrips.isNotEmpty()) {
                    item {
                        TripCard(
                            upcomingTrips.first(),
                            viewModel,
                            travelerId,
                            navController,
                            onDeleteSuccess = { showSnackbarMessage = "Trip deleted successfully!" })
                    }
                    if (showAllUpcoming) {
                        items(upcomingTrips.drop(1)) { trip ->
                            TripCard(
                                trip,
                                viewModel,
                                travelerId,
                                navController,
                                onDeleteSuccess = {
                                    showSnackbarMessage = "Trip deleted successfully!"
                                })
                        }
                    }
                } else {
                    item { EmptyTripCard("No Upcoming Trips!") }
                }

                /*Previous Trips*/
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Previous Trips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        if (previousTrips.size > 1) {
                            Text(
                                text = if (showAllPrevious) "View less" else "View all",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray,
                                modifier = Modifier.clickable { showAllPrevious = !showAllPrevious }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (previousTrips.isNotEmpty()) {
                    item {
                        TripCard(
                            previousTrips.first(),
                            viewModel,
                            travelerId,
                            navController,
                            onDeleteSuccess = { showSnackbarMessage = "Trip deleted successfully!" })
                    }
                    if (showAllPrevious) {
                        items(previousTrips.drop(1)) { trip ->
                            TripCard(
                                trip,
                                viewModel,
                                travelerId,
                                navController,
                                onDeleteSuccess = {
                                    showSnackbarMessage = "Trip deleted successfully!"
                                })
                        }
                    }
                } else {
                    item { EmptyTripCard("No Previous Trips!") }
                }
            }
        }


        if (showSnackbarMessage != null) {
            LaunchedEffect(showSnackbarMessage) {
                delay(5000)
                showSnackbarMessage = null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Orange
                        )

                        // Message (Center)
                        Text(
                            showSnackbarMessage!!,
                            color = Color(0xFF5F6368),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { showSnackbarMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateTripDialog(
            onDismiss = { showDialog = false },
            viewModel = viewModel,
            travelerId = travelerId,
            onSuccess = {
                showSnackbarMessage = "Trip added successfully!"
            },
            navController = navController,
            recommendationViewModel = recommendationViewModel
        )
    }
}




@Composable
fun CreateTripDialog(viewModel: dbViewModel, travelerId: String,
                     onDismiss: () -> Unit,onSuccess: () -> Unit,
                     navController: NavController,
                     recommendationViewModel: RecommendationViewModel) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tripName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var tripNameIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var startDateIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var endDateIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var endDateIsBeforeStartDateMessage by remember { mutableStateOf<String?>(null) }
    var tripNameExistsMessage by remember { mutableStateOf<String?>(null) }
    val tripNameLimit = 50
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }




    fun collectAllTripNames(): MutableList<String>{
        val allTrips: MutableList<Trip> = mutableListOf()
        val allTripNames : MutableList<String> = mutableListOf()

        if (viewModel.currentTrips.value.isNotEmpty()){
            for (i in viewModel.currentTrips.value){
                allTrips.add(i)
            }
        }

        if (viewModel.previousTrips.value.isNotEmpty()){
            for (i in viewModel.previousTrips.value){
                allTrips.add(i)
            }
        }

        if (viewModel.upcomingTrips.value.isNotEmpty()){
            for (i in viewModel.upcomingTrips.value){
                allTrips.add(i)
            }
        }

        if (allTrips.isNotEmpty()){
            for (i in allTrips)
                allTripNames.add(i.name.lowercase())
        }

        return allTripNames
    }

    fun checkIfNameExists(tripNamesList: MutableList<String>, tripName: String): Boolean{
        if (tripNamesList.isNotEmpty()){
            if (tripNamesList.contains(tripName.lowercase()))
                return true
            else
                return false
        }
        else
            return false
    }


    fun tripEndDateIsAfterStartDate(startDate: String, endDate: String): Boolean{
        val newStartDate = startDate
        val newEndDate = endDate
        // start split
        val splitStartDate=newStartDate.split('-')
        val splitStartDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitStartDate){
            splitStartDateToInt.add(i.toInt())
        }
        // end split
        val splitEndDate=newEndDate.split('-')
        val splitEndDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitEndDate){
            splitEndDateToInt.add(i.toInt())
        }

        val startDateCalenderInstance = Calendar.getInstance()
        val endDateCalenderInstance = Calendar.getInstance()
        startDateCalenderInstance.set(splitStartDateToInt[0],splitStartDateToInt[1] - 1,
            splitStartDateToInt[2],0,0,0 )

        endDateCalenderInstance.set(splitEndDateToInt[0],splitEndDateToInt[1] - 1,
            splitEndDateToInt[2],0,0,0 )

        return endDateCalenderInstance.after(startDateCalenderInstance)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .padding(16.dp)
            .width(380.dp),
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "New Trip", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.LightGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    //uploading img
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri!!),
                            contentDescription = "Selected Trip Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "Upload Image", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxWidth().height(110.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = tripName,
                        onValueChange = { input ->
                            if (input.length <= tripNameLimit) {
                                tripName = input
                            }
                            if (tripName.isNotBlank()) {
                                tripNameIsEmptyMessage = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = {
                            Text(
                                text = "Trip name", color = Color.Gray, fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        isError = tripNameIsEmptyMessage != null || tripNameExistsMessage != null,
                        supportingText = {
                            if (tripNameIsEmptyMessage != null) {
                                Text(
                                    modifier = Modifier.height(40.dp),
                                    text = tripNameIsEmptyMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (tripNameExistsMessage != null) {
                                Text(
                                    modifier = Modifier.height(40.dp),
                                    text = tripNameExistsMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

               // Spacer(modifier = Modifier.height(8.dp))

                StartDatePickerButton(
                    label = "Start Date",
                    selectedDate = startDate,
                    errorMessage = startDateIsEmptyMessage,
                    setStartDate = { startDate = it },
                    setErrorMessage = { startDateIsEmptyMessage = it }
                )


                Spacer(modifier = Modifier.height(8.dp))

                EndDatePickerButton(
                    label = "End Date",
                    selectedDate = endDate,
                    emptyErrorMessage = endDateIsEmptyMessage,
                    endDateBeforeEmptyMessage = endDateIsBeforeStartDateMessage,
                    setEndDate = { endDate = it },
                    setEmptyErrorMessage = { endDateIsEmptyMessage = it }
                )

            }
        },

        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val coroutineScope = rememberCoroutineScope()
                Button(
                    onClick = {
                        //input validation
                        val allTripNames = collectAllTripNames()
                        if (tripName.isBlank()){tripNameIsEmptyMessage= "Enter trip name"}
                        if (startDate.isBlank()){startDateIsEmptyMessage= "Enter start date"}
                        if (startDate != ""){startDateIsEmptyMessage=null}
                        if (endDate.isBlank()){endDateIsEmptyMessage= "Enter end date"}
                        if (endDate != ""){endDateIsEmptyMessage=null}
                        if (startDate.isNotBlank() && endDate.isNotBlank()){
                            if (!tripEndDateIsAfterStartDate(startDate, endDate)){
                                endDateIsBeforeStartDateMessage="End date must be after start date"
                            }
                        }
                        if (tripName.isNotBlank() && checkIfNameExists(allTripNames,tripName) ){
                            tripNameExistsMessage = "Name already exists, try a different name"
                        }
                        if (tripName.isNotBlank() && !checkIfNameExists(allTripNames,tripName) ){
                            tripNameExistsMessage = null
                        }
                        if (startDate.isNotBlank() && endDate.isNotBlank()){
                            if (tripEndDateIsAfterStartDate(startDate, endDate)){
                                endDateIsBeforeStartDateMessage=null
                            }
                        }
                        if (tripName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
                            && tripEndDateIsAfterStartDate(startDate, endDate) &&
                            !checkIfNameExists(allTripNames, tripName)) {

                            val imageUrl =imageUri?.let { uriToBase64IfSmallEnough(context, it) }
                            // Handle trip creation using tripName, startDate, and endDate

//                            viewModel.addTrip(travelerId,tripName,startDate,endDate,imageUrl)
//                            onSuccess()
//                            onDismiss()
                            coroutineScope.launch {
                                isSaving = true
                                viewModel.addTrip(travelerId, tripName, startDate, endDate, imageUrl)
                                delay(300)
                                isSaving = false
                                onSuccess()
                                onDismiss()
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = "Start Planning", fontSize = 14.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val allTripNames = collectAllTripNames()
                        if (tripName.isBlank()){tripNameIsEmptyMessage= "Enter trip name"}
                        if (startDate.isBlank()){startDateIsEmptyMessage= "Enter start date"}
                        if (startDate != ""){startDateIsEmptyMessage=null}
                        if (endDate.isBlank()){endDateIsEmptyMessage= "Enter end date"}
                        if (endDate != ""){endDateIsEmptyMessage=null}
                        if (startDate.isNotBlank() && endDate.isNotBlank()){
                            if (!tripEndDateIsAfterStartDate(startDate, endDate)){
                                endDateIsBeforeStartDateMessage="End date must be after start date"
                            }
                        }
                        if (tripName.isNotBlank() && checkIfNameExists(allTripNames,tripName) ){
                            tripNameExistsMessage = "Name already exists, try a different name"
                        }
                        if (tripName.isNotBlank() && !checkIfNameExists(allTripNames,tripName) ){
                            tripNameExistsMessage = null
                        }
                        if (startDate.isNotBlank() && endDate.isNotBlank()){
                            if (tripEndDateIsAfterStartDate(startDate, endDate)){
                                endDateIsBeforeStartDateMessage=null
                            }
                        }
                        if (tripName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank()
                            && tripEndDateIsAfterStartDate(startDate, endDate) &&
                            !checkIfNameExists(allTripNames, tripName)) {
                            // saving values in vm for trip creation
                            recommendationViewModel.setStartDate(startDate)
                            recommendationViewModel.setEndDate(endDate)
                            recommendationViewModel.setTripName(tripName)
                            val imageUrl =imageUri?.let { uriToBase64IfSmallEnough(context, it) }
                            recommendationViewModel.setImageUri(imageUrl)
                            recommendationViewModel.getRecommendationWithFlight(
                                date = startDate,
                                travelerId = travelerId
                            )
                            navController.navigate(Screens.Recommendation.route)
                        }
                              },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text(text = "Recommend a trip", fontSize = 14.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.width(290.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text(text = "Cancel", color = Color.White)
                    }
                }
            }
        }
    )
}

@Composable
fun DeleteTripConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Delete Trip?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This will delete all the trip details permanently.",
                    color = Color(0xFF757575), // Dark Gray
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    )
}

@Composable
fun StartDatePickerButton(
    label: String,
    selectedDate: String,
    errorMessage: String?,
    setStartDate: (String) -> Unit,
    setErrorMessage: (String?) -> Unit) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
            setStartDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {
            setStartDate(selectedDate)
            if (selectedDate.isNotBlank()){
                setErrorMessage(null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        datePickerDialog.show()
                    }
                }
            },
        label = {
            Text(
                text = label, color = Color.Gray, fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(12),
        maxLines = 1,
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
        isError = errorMessage != null,
        supportingText = {
            if (errorMessage != null){
                Text(
                    modifier = Modifier.height(35.dp),
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun EndDatePickerButton(
    label: String,
    selectedDate: String,
    emptyErrorMessage: String?,
    endDateBeforeEmptyMessage: String?,
    setEndDate: (String) -> Unit,
    setEmptyErrorMessage: (String?) -> Unit
){

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
            setEndDate(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    OutlinedTextField(
        value = selectedDate,
        onValueChange = {
            setEndDate(selectedDate)
            if (selectedDate.isNotBlank()){
                setEmptyErrorMessage(null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(selectedDate) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        datePickerDialog.show()
                    }
                }
            },
        label = {
            Text(
                text = label, color = Color.Gray, fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        },
        shape = RoundedCornerShape(12),
        maxLines = 1,
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
        isError = emptyErrorMessage != null || endDateBeforeEmptyMessage != null,
        supportingText = {
            if (emptyErrorMessage != null){
                Text(
                    modifier = Modifier.height(35.dp),
                    text = emptyErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (endDateBeforeEmptyMessage != null){
                Text(
                    modifier = Modifier.height(35.dp),
                    text = endDateBeforeEmptyMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
@Composable
fun TripCard(
    trip: Trip,
    viewModel: dbViewModel,
    travelerId: String,
    navController: NavController,
    onDeleteSuccess: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(350.dp)
            .height(160.dp)
            .clickable {
                navController.navigate("itinerary/${trip.tripId}/${trip.itineraryId}/${trip.name}")
                //navController.navigate("itinerary/${trip.tripId}/${trip.name}") // Pass tripId and tripName
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                val base64Image = trip.imageUrl
                if (!base64Image.isNullOrEmpty()) {
                    val imageBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = trip.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.empty_trip),
                        contentDescription = trip.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }


                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = trip.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${trip.startDate} - ${trip.endDate}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteTripConfirmationDialog(
            onConfirm = {
                viewModel.deleteTrip(trip.tripId, travelerId)
                onDeleteSuccess()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}







@Composable
fun EmptyTripCard(message: String) {
    Card(
        modifier = Modifier
            .width(350.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        }
    }
}


