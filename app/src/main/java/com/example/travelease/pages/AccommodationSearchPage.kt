package com.example.travelease.pages

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.R
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.fromToList
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AccommodationSearchSection(
    navController: NavController,
    viewModel: AccommodationViewModel,
    travelerId: String, tripId: String, itineraryId: String,
    mode: String = "selectTrip"
) {
    var destinationIsEmptyMessage by remember { mutableStateOf("") }
    var checkInDateIsEmptyMessage by remember { mutableStateOf("") }
    var checkOutDateIsEmptyMessage by remember { mutableStateOf("") }

    fun checkInDateIsGreaterThatCheckOutDate(checkIn : String, checkOut : String): Boolean{

        var newCheckIn = checkIn
        var newCheckOut = checkOut
        // checkIn split
        var splitCheckInDate=newCheckIn.split('-')
        val splitCheckInDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitCheckInDate){
            splitCheckInDateToInt.add(i.toInt())
        }
        // checkOut split
        var splitCheckOutDate=newCheckOut.split('-')
        val splitCheckOutDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitCheckOutDate){
            splitCheckOutDateToInt.add(i.toInt())
        }

        var checkInCalenderInstance = Calendar.getInstance()
        var checkOutCalenderInstance = Calendar.getInstance()
        checkInCalenderInstance.set(splitCheckInDateToInt[0],splitCheckInDateToInt[1] - 1,
            splitCheckInDateToInt[2],0,0,0 )

        checkOutCalenderInstance.set(splitCheckOutDateToInt[0],splitCheckOutDateToInt[1] - 1,
            splitCheckOutDateToInt[2],0,0,0 )

        return checkOutCalenderInstance.after(checkInCalenderInstance)
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }


    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center) {

        Text(
            text = "Select your city",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            fontFamily = DMSansFontFamily
        )

        Spacer(modifier = Modifier.height(10.dp))


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {



                OutlinedButton(
                    onClick = { viewModel.expanded.value = !viewModel.expanded.value },
                    shape = RoundedCornerShape(12),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.shadow(5.dp, RoundedCornerShape(5.dp))
                        .height(60.dp)
                        .width(200.dp)
                ) {
                    Text(
                        text = viewModel.destination.value,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = DMSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                DropdownMenu(
                    expanded = viewModel.expanded.value,
                    onDismissRequest = { viewModel.expanded.value = false },
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp),
                    containerColor = Color.White,
                ) {
                    fromToList.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    fontFamily = DMSansFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = {
                                viewModel.destination.value = option
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 110.dp)
            ,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = destinationIsEmptyMessage, color= MaterialTheme.colorScheme.error,
                fontSize = 14.sp, textAlign = TextAlign.Left)
        }



        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Select your stay dates",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
            fontFamily = DMSansFontFamily
        )


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier
                .weight(1f)
                .padding(8.dp)) {
                CheckInDatePickerButton(label = "Check-In Date",
                    selectedDate = viewModel.checkInDate,
                    viewModel :: setCheckInDate)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier
                .weight(1f)
                .padding(8.dp)) {
                CheckOutDatePickerButton(label = "Check-Out Date",
                    selectedDate = viewModel.checkOutDate,
                    viewModel :: setCheckOutDate)
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp)
            ) {
                Text(text = checkInDateIsEmptyMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            ) {
                Text(text = checkOutDateIsEmptyMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                if (viewModel.destination.value == "Destination"){
                    destinationIsEmptyMessage="Enter a Destination"}
                if (viewModel.checkInDate.value == ""){
                    checkInDateIsEmptyMessage="Enter a check-in date"}
                if (viewModel.checkOutDate.value == ""){
                    checkOutDateIsEmptyMessage="Enter a check-out date"}
                if (viewModel.destination.value != "Destination"){
                    destinationIsEmptyMessage=""}
                if (viewModel.checkInDate.value != ""){
                    checkInDateIsEmptyMessage=""}
                if (viewModel.checkOutDate.value != ""){
                    checkOutDateIsEmptyMessage=""}
                if(viewModel.checkOutDate.value != "" &&
                    viewModel.checkInDate.value != "" &&
                    !checkInDateIsGreaterThatCheckOutDate(
                        convertLongToTime(convertDateToLong(viewModel.checkInDate.value)),
                        convertLongToTime(convertDateToLong(viewModel.checkOutDate.value)))){
                    checkOutDateIsEmptyMessage="Check-out must be after check-in date"
                }
                if(viewModel.checkOutDate.value != "" &&
                    viewModel.checkInDate.value != "" &&
                    checkInDateIsGreaterThatCheckOutDate(
                        convertLongToTime(convertDateToLong(viewModel.checkInDate.value)),
                        convertLongToTime(convertDateToLong(viewModel.checkOutDate.value)))){
                    checkOutDateIsEmptyMessage=""
                }
                if (viewModel.destination.value != "Destination" &&
                    viewModel.checkInDate.value != "" &&
                    viewModel.checkOutDate.value != "" && checkInDateIsGreaterThatCheckOutDate(
                        convertLongToTime(convertDateToLong(viewModel.checkInDate.value)),
                        convertLongToTime(convertDateToLong(viewModel.checkOutDate.value))))
                {
                    viewModel.getAccommodations()
                    Log.d("NavigationDebug", "Checking values before navigation:")
                    Log.d("NavigationDebug", "Traveler ID: $travelerId")
                    Log.d("NavigationDebug", "Trip ID: $tripId")
                    Log.d("NavigationDebug", "Itinerary ID: $itineraryId")
                    //navController.navigate(Screens.Accommodations.route)
                    val safeTripId = if (tripId.isBlank()) "INVALID_ID" else tripId
                    val safeItineraryId = if (itineraryId.isBlank()) "INVALID_ID" else itineraryId
                    val newMode = if (tripId.isNotBlank() && itineraryId.isNotBlank()) "specificTrip" else "selectTrip"
                    val route = "accommodation_screen?tripId=$safeTripId&itineraryId=$safeItineraryId&mode=$newMode"
                    navController.navigate(route)

                }

                      },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(120.dp)
                .height(50.dp)
                .padding(top = 8.dp)
                .align(alignment = Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
        ) {
            Text(
                text = "Search", fontSize = 18.sp, color = Color.White,
                fontFamily = DMSansFontFamily, fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun CheckInDatePickerButton(label: String,
                            selectedDate: MutableState<String>
                            , setCheckInDate:(String)-> Unit) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            //selectedDate.value = "$day ${getMonthName(month)} $year"
            selectedDate.value = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)

        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    OutlinedTextField(
        value = selectedDate.value,
        onValueChange = {
            selectedDate.value = it
            setCheckInDate(selectedDate.value)

        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
        modifier = Modifier
            .size(width = 335.dp, height = 70.dp)
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
                text = label, color = Grey, fontSize = 11.sp,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal
            )
        },
        maxLines = 1,
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") }
    )

}

@Composable
fun CheckOutDatePickerButton(label: String,
                             selectedDate: MutableState<String>
                             , setCheckOutDate:(String)-> Unit) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            //selectedDate.value = "$day ${getMonthName(month)} $year"
            selectedDate.value=String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis // disables past dates

    OutlinedTextField(
        value = selectedDate.value,
        onValueChange = {
            selectedDate.value = it
            setCheckOutDate(selectedDate.value)

        },
        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
        modifier = Modifier
            .size(width = 335.dp, height = 70.dp)
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
                text = label, color = Grey, fontSize = 11.sp,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal
            )
        },
        maxLines = 1,
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") }
    )

}



