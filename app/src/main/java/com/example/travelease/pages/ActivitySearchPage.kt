package com.example.travelease.pages

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Activity
import com.example.travelease.firebaseDB.entities.Trip
import com.example.travelease.R
import com.example.travelease.fromToList
import com.example.travelease.getMonthName
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


@Composable
fun ActivitiesSearchSection(
    navController: NavController,
    viewModel: ActivitiesViewModel,
    travelerId: String, tripId: String, itineraryId: String,dbViewModel: dbViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("Select a City") }
    val trips = dbViewModel.currentTrips.collectAsState().value + dbViewModel.upcomingTrips.collectAsState().value
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }
    var showTripDropdown by remember { mutableStateOf(false) }
   // var tripIdForInsideTripNavigation by remember { mutableStateOf<String?>(null) }
    var cityIsEmptyMessage by remember { mutableStateOf("") }

    fun endDateIsAfterStartDate(startDate: String, endDate: String): Boolean{
        var newStartDate = startDate
        var newEndDate = endDate
        // start split
        var splitStartDate=newStartDate.split('-')
        val splitStartDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitStartDate){
            splitStartDateToInt.add(i.toInt())
        }
        // end split
        var splitEndDate=newEndDate.split('-')
        val splitEndDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitEndDate){
            splitEndDateToInt.add(i.toInt())
        }

        var startDateCalenderInstance = Calendar.getInstance()
        var endDateCalenderInstance = Calendar.getInstance()
        startDateCalenderInstance.set(splitStartDateToInt[0],splitStartDateToInt[1] - 1,
            splitStartDateToInt[2],0,0,0 )

        endDateCalenderInstance.set(splitEndDateToInt[0],splitEndDateToInt[1] - 1,
            splitEndDateToInt[2],0,0,0 )

        val isSameDay: Boolean = DateUtils.isSameDay(
            startDateCalenderInstance, endDateCalenderInstance)

        return endDateCalenderInstance.after(startDateCalenderInstance) || isSameDay
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    var showDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    )
    {

        Text(
            text = "Add a custom activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Orange,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            fontFamily = DMSansFontFamily,
        )

        Spacer(modifier = Modifier.height(5.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), horizontalArrangement = Arrangement.Center) {
            CustomActivityNameTextField(
                label = "Activity Name",
                name = viewModel.customActivityName,
                errorMessage = viewModel.customActivityNameIsEmptyMessage,
                viewModel::setCustomActivityName,
                viewModel::setCustomActivityNameErrorMessage
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier
                .weight(1f)
                .padding(8.dp)) {
                ActivityStartDatePickerButton(
                    label = "Start Date",
                    selectedDate = viewModel.startDate,
                    errorMessage = viewModel.startDateIsEmptyMessage,
                    setStartDate = viewModel::setStartDate,
                    setErrorMessage = viewModel::setStartDateErrorMessage
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier
                .weight(1f)
                .padding(8.dp)) {
                ActivityEndDatePickerButton(
                    label = "End Date",
                    selectedDate = viewModel.endDate,
                    emptyErrorMessage = viewModel.endDateIsEmptyMessage,
                    endDateBeforeErrorMessage = viewModel.endDateIsBeforeStartDateErrorMessage,
                    setEndDate = viewModel::setEndDate,
                    setEndDateErrorMessage = viewModel::setEndDateErrorMessage,
                    setEndDateIsAfterStartDateErrorMessage = viewModel::setEndDateIsBeforeStartDateErrorMessage
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    if (viewModel.customActivityName.value.isBlank()) {
                        viewModel.setCustomActivityNameErrorMessage("Enter the activity name")
                    }
                    if (viewModel.startDate.value.isBlank()) {
                        viewModel.setStartDateErrorMessage("Enter a start date")
                    }
                    if (viewModel.startDate.value != "") {
                        viewModel.setStartDateErrorMessage(null)
                    }
                    if (viewModel.endDate.value.isBlank()) {
                        viewModel.setEndDateErrorMessage("Enter an end date")
                    }
                    if (viewModel.endDate.value != "") {
                        viewModel.setEndDateErrorMessage(null)
                    }
                    if (viewModel.endDate.value.isNotBlank() && viewModel.startDate.value.isNotBlank()) {
                        if (!endDateIsAfterStartDate(
                                convertLongToTime(convertDateToLong(viewModel.startDate.value)),
                                convertLongToTime(convertDateToLong(viewModel.endDate.value))
                            )
                        ) {
                            viewModel.setEndDateIsBeforeStartDateErrorMessage("End date must be after start date")
                        }
                    }
                    if (viewModel.endDate.value.isNotBlank() && viewModel.startDate.value.isNotBlank()) {
                        if (endDateIsAfterStartDate(
                                convertLongToTime(convertDateToLong(viewModel.startDate.value)),
                                convertLongToTime(convertDateToLong(viewModel.endDate.value))
                            )
                        ) {
                            viewModel.setEndDateIsBeforeStartDateErrorMessage(null)
                        }
                    }
                    if (viewModel.customActivityName.value.isNotBlank() &&
                        viewModel.startDate.value.isNotBlank() &&
                        viewModel.endDate.value.isNotBlank() &&
                        endDateIsAfterStartDate(
                            convertLongToTime(convertDateToLong(viewModel.startDate.value)),
                            convertLongToTime(convertDateToLong(viewModel.endDate.value))
                        )
                    ) {
                        //  showDialog = true
                        if (tripId == "" || itineraryId == "") { // handling navigating from General search
                            showTripDropdown = true
                        } else {
                            showDialog = true

                        }
                    }

                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Text(
                    text = "Add",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showTripDropdown) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { showTripDropdown = false },
                    containerColor = Color.White,
                    modifier = Modifier.height(200.dp),
                    offset = DpOffset(x = 150.dp, y = 0.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Choose a trip", fontWeight = FontWeight.Bold) },
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


        // Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                title = {
                    if (!showSuccessAnimation) {
                        Text(
                            text = "Add Custom Activity?",
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
                                colors = androidx. compose. material.ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
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
                                            name = viewModel.customActivityName.value,
                                            sdate = viewModel.startDate.value,
                                            edate = viewModel.endDate.value,
                                            activityId = UUID.randomUUID().toString(),
                                            itineraryId = selectedTrip?.itineraryId ?: ""
                                        )

                                        dbViewModel.addActivityToItinerary(
                                            travelerId,
                                            itineraryId = selectedTrip?.itineraryId,
                                            activity = activityData,
                                            onSuccess = { Log.d("Firestore", "Activity added successfully!") },
                                            onFailure = { e: Exception -> Log.e("Firestore", "Error: ${e.message}") },
                                            tripId = selectedTrip?.tripId ?: ""
                                        )
                                    }
                                    else{
                                        val activityData = Activity(
                                            name = viewModel.customActivityName.value,
                                            sdate = viewModel.startDate.value,
                                            edate = viewModel.endDate.value,
                                            activityId = UUID.randomUUID().toString(),
                                            itineraryId = itineraryId
                                        )

                                        dbViewModel.addActivityToItinerary(
                                            travelerId,
                                            itineraryId = itineraryId,
                                            activity = activityData,
                                            onSuccess = { Log.d("Firestore", "Activity added successfully!") },
                                            onFailure = { e: Exception -> Log.e("Firestore", "Error: ${e.message}") },
                                            tripId = tripId
                                        )
                                    }



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
                                Text("Add", color = Color.White)
                            }
                        }
                    }
                }
            )
        }

    // end of column scope (previous)
    Spacer(modifier = Modifier.height(15.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Or search for an activity!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Orange,
                fontFamily = DMSansFontFamily
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = "Select a city",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = DMSansFontFamily
            )
        }


    Spacer(modifier = Modifier.height(10.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(12),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .height(60.dp)
                    .width(200.dp)
            ) {
                Text(
                    text = selectedCity,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = DMSansFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp),
                containerColor = Color.White
            ) {
                fromToList.forEach { city ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = city,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontFamily = DMSansFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            selectedCity = city
                            viewModel.setSearchActivityName(city)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    if (cityIsEmptyMessage.isNotEmpty()) {
        Text(
            text = cityIsEmptyMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 14.sp,
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 120.dp, top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (selectedCity == "Select a City") {
                        cityIsEmptyMessage = "Please select a city"
                    } else {
                        cityIsEmptyMessage = ""
                        viewModel.setSearchActivityName(selectedCity)
                        viewModel.getActivities(viewModel.searchActivityName.value)
                        val route = "activity_screen/$tripId/$itineraryId"
                        navController.navigate(route)
                    }
                    if (viewModel.searchActivityName.value.isBlank() ||
                        selectedCity == "Select a City") {
                        viewModel.setSearchActivityNameErrorMessage("Enter a location")
                    }
                    if (viewModel.searchActivityName.value.isNotBlank() &&
                        selectedCity != "Select a City") {
                        viewModel.getActivities(viewModel.searchActivityName.value)
                        val route = "activity_screen/$tripId/$itineraryId"
                        navController.navigate(route)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Text(
                    text = "Search",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun ActivityStartDatePickerButton(label: String,
                                  selectedDate: MutableState<String>,
                                  errorMessage: MutableState<String?>,
                                  setStartDate:(String)-> Unit,
                                  setErrorMessage: (String?) -> Unit) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            selectedDate.value = "$day ${getMonthName(month)} $year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    Column(modifier = Modifier.width(335.dp)) {

        OutlinedTextField(
            value = selectedDate.value,
            onValueChange = {
                selectedDate.value = it
                setStartDate(selectedDate.value)

                if (selectedDate.value.isNotBlank()){
                    setErrorMessage(null)
                }

            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
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
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = label, color = Grey, fontSize = 14.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            maxLines = 1,
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
            isError = errorMessage.value != null
//            supportingText = {
//                if(errorMessage.value != null){
//                    Text(
//                        modifier = Modifier.fillMaxSize(),
//                        text = errorMessage.value ?: "",
//                        color = MaterialTheme.colorScheme.error
//                    )
//                }
//            }
        )
        if(errorMessage.value != null){
            Text(
                modifier = Modifier
                    .height(45.dp)
                    .padding(start = 16.dp),
                text = errorMessage.value ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActivityEndDatePickerButton(label: String,
                                selectedDate: MutableState<String>,
                                emptyErrorMessage: MutableState<String?>,
                                endDateBeforeErrorMessage : MutableState<String?>,
                                setEndDate: (String) -> Unit,
                                setEndDateErrorMessage: (String?) -> Unit,
                                setEndDateIsAfterStartDateErrorMessage: (String?) -> Unit) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            selectedDate.value = "$day ${getMonthName(month)} $year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    Column(modifier = Modifier.width(335.dp)) {

        OutlinedTextField(
            value = selectedDate.value,
            onValueChange = {
                selectedDate.value = it
                setEndDate(selectedDate.value)
                if (selectedDate.value.isNotBlank()){
                    setEndDateErrorMessage(null)
                }

            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
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
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = label, color = Grey, fontSize = 13.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            maxLines = 1,
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
            isError = emptyErrorMessage.value != null || endDateBeforeErrorMessage.value != null
        )
        if(emptyErrorMessage.value != null){
            Text(
                modifier = Modifier
                    .height(45.dp)
                    .padding(start = 16.dp),
                text = emptyErrorMessage.value ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
        if (endDateBeforeErrorMessage.value != null){
            Text(
                modifier = Modifier
                    .height(45.dp)
                    .padding(start = 8.dp),
                text = endDateBeforeErrorMessage.value ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CustomActivityNameTextField(label: String,
                                name:  MutableState<String>,
                                errorMessage: MutableState<String?>,
                                setCustomActivityName: (String) -> Unit,
                                setCustomActivityNameErrorMessage: (String?) -> Unit) {

    OutlinedTextField(
        value = name.value,
        onValueChange ={ input ->
            if(input.all { !it.isDigit() }) {
                name.value = input
                setCustomActivityName(name.value)
            }
            if (name.value.isNotBlank()){
                setCustomActivityNameErrorMessage(null)
            }
        },
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier
            .width(400.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OceanBlue,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black
        ),
        isError = errorMessage.value != null,
        supportingText = {
            if (errorMessage.value != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = errorMessage.value ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

    )
}

