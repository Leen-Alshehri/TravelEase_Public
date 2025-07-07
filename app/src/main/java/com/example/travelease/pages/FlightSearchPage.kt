package com.example.travelease.pages

import android.app.DatePickerDialog
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
import android.util.Log
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.fromToList
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import java.util.Calendar
import java.util.Locale

@Composable
fun FlightSearchSection(
    navController: NavController, viewModel: MainViewModel,travelerId: String, tripId: String, itineraryId: String) {
    var fromIsEmptyMessage by remember { mutableStateOf("") }
    var toIsEmptyMessage by remember { mutableStateOf("") }
    var startDateIsEmptyMessage by remember { mutableStateOf("") }



    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

        // row for "from" and "to" labels
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                Text(
                    text = "From",
                    fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier.weight(1f).padding(8.dp)
            ) {
                Text(
                    text = "To",
                    fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        //dropdown Buttons for from and to
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //departure selection
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.expanded.value = !viewModel.expanded.value },
                    shape = RoundedCornerShape(12),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.shadow(5.dp, RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = viewModel.fromText.value, color = Color.Black, textAlign = TextAlign.Center, fontFamily = DMSansFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal
                    )
                }
                DropdownMenu(
                    expanded = viewModel.expanded.value, onDismissRequest = { viewModel.expanded.value = false }, modifier = Modifier.width(160.dp).height(300.dp), containerColor = Color.White,
                ) {
                    val fromToList1 = fromToList.toMutableList() // Copy list to modify it
                    if (viewModel.toText.value != "Destination") fromToList1.remove(viewModel.toText.value)

                    fromToList1.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                                    fontFamily = DMSansFontFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                                )
                            },
                            onClick = { viewModel.fromText.value = option }
                        )
                    }
                }

            }

            //switcher button for swapping from and to values
            Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = {
                    if (viewModel.fromText.value != "Departure" && viewModel.toText.value != "Destination") {
                        val from = viewModel.fromText.value
                        val to = viewModel.toText.value
                        viewModel.fromText.value = to
                        viewModel.toText.value = from
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.SyncAlt,
                        contentDescription = "Switch Search entries",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            //destination selection
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.expanded2.value = !viewModel.expanded2.value },
                    shape = RoundedCornerShape(12),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.shadow(5.dp, RoundedCornerShape(5.dp))
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = viewModel.toText.value,
                        color = Color.Black,
                        fontFamily = DMSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                DropdownMenu(
                    expanded = viewModel.expanded2.value,
                    onDismissRequest = { viewModel.expanded2.value = false },
                    modifier = Modifier.width(160.dp).height(300.dp),
                    containerColor = Color.White
                ) {
                    val fromToList2 = fromToList.toMutableList()
                    if (viewModel.fromText.value != "Departure") fromToList2.remove(viewModel.fromText.value)

                    fromToList2.forEach { option ->
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
                            onClick = { viewModel.toText.value = option }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (fromIsEmptyMessage.isNotEmpty()) {
                    Text(
                        text = fromIsEmptyMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                if (toIsEmptyMessage.isNotEmpty()) {
                    Text(
                        text = toIsEmptyMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //date field spans below from and to
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Date",
                fontSize = 16.sp,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                DatePickerButton(
                    label = "Departure Date",
                    selectedDate = viewModel.flightStartDate,
                    setStartDateFlight = viewModel::setStartDateForFlight,
                    modifier = Modifier
                        .width(400.dp)
                        .height(60.dp)
                )

            }
            if (startDateIsEmptyMessage.isNotEmpty()) {
                Text(
                    text = startDateIsEmptyMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

        }

        Spacer(modifier = Modifier.height(24.dp))

        //center the search button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (viewModel.fromText.value == "Departure") fromIsEmptyMessage =
                        "Enter a departure"
                    if (viewModel.toText.value == "Destination") toIsEmptyMessage =
                        "Enter a destination"
                    if (viewModel.flightStartDate.value.isEmpty()) startDateIsEmptyMessage =
                        "Enter a departure date"

                    if (viewModel.fromText.value != "Departure") fromIsEmptyMessage = ""
                    if (viewModel.toText.value != "Destination") toIsEmptyMessage = ""
                    if (viewModel.flightStartDate.value.isNotEmpty()) startDateIsEmptyMessage = ""

                    if (viewModel.fromText.value != "Departure" &&
                        viewModel.toText.value != "Destination" &&
                        viewModel.flightStartDate.value.isNotEmpty()
                    ) {
                        viewModel.getFlights()
                        //navController.navigate(Screens.Flights.route)
                        Log.d("NavigationDebug", "Checking values before navigation:")
                        Log.d("NavigationDebug", "Traveler ID: $travelerId")
                        Log.d("NavigationDebug", "Trip ID: $tripId")
                        Log.d("NavigationDebug", "Itinerary ID: $itineraryId")

                        val route = if (tripId == "INVALID_ID" || itineraryId == "INVALID_ID" || tripId.isEmpty() || itineraryId.isEmpty()) {
                            "flight_screen?tripId=&itineraryId=&mode=selectTrip"
                        } else {
                            "flight_screen?tripId=$tripId&itineraryId=$itineraryId&mode=specificTrip"
                        }


                        Log.d("NavigationDebug", "Navigating to: $route")
                        navController.navigate(route)

                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(50.dp)
                    .padding(top = 8.dp),
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
fun DatePickerButton(label: String,
                     selectedDate: MutableState<String>
                     ,setStartDateFlight:(String)-> Unit,
                     modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            selectedDate.value = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis //disables past dates

    OutlinedTextField(
        value = selectedDate.value,
        onValueChange = {
            selectedDate.value = it
            setStartDateFlight(selectedDate.value)

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
                text = label, color = Grey, fontSize = 16.sp, fontFamily = DMSansFontFamily, fontWeight = FontWeight.Normal
            )
        },
        maxLines = 1,
        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") }
    )

}

