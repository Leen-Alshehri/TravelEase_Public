package com.example.travelease.pages

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.AuthState
import com.example.travelease.AuthViewModel
import com.example.travelease.R
import com.example.travelease.getMonthName
import com.example.travelease.navigation.AuthScreens
import com.example.travelease.navigation.Graph
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

// mutableStateOf
    //var showModal by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isPasswordLengthExceedsLimit by remember { mutableStateOf(false) }
    var isEmailFormatValid by remember { mutableStateOf(false) }
    var isNumberFormatValid by remember { mutableStateOf(false) }
    var isPasswordMinLengthValid by remember { mutableStateOf(false) }
    var isUserOver18 by remember{ mutableStateOf(false) }
    var passwordMinLengthMessage by remember { mutableStateOf<String?>(null) }
    // var nameContainsNumberMessage by remember { mutableStateOf<String?>(null) }
    var emailIsInvalidMessage by remember { mutableStateOf<String?>(null) }
    var nameIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var emailIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var phoneNumberIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var phoneNumberIsNotValidMessage by remember { mutableStateOf<String?>(null) }
    var DOBIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var userIsNot18Message by remember { mutableStateOf<String?>(null) }
    var passwordMustBeGrayMessage = "Password must be at least 8 characters long"


//Regular Expressions
    val nameReg = "\\d"
    val emailReg = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
//    val numberReg="^0[5][\\d]{8}"
    val numberReg = "^(?![123456789])[0-9](?![123467890])[0-9]"

//LengthLimits
    val nameLimit = 50
    val emailLimit = 35
    val phoneNumberLimit = 10
    val passwordLimit = 15

    fun convertDobDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    fun convertDobLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    //UI validation methods
    fun validateLength(text: String, limit: Int) {
        if (limit == passwordLimit) {
            isPasswordLengthExceedsLimit = text.length > limit
        }
    }

    fun validateEmail(email: String) {
        isEmailFormatValid = !email.contains(Regex(emailReg))
    }

    fun isUserOver18(date: String): Boolean {
        var newDate=date
        var splitDate=newDate.split('-')
        val splitDateToInt : MutableList<Int> = mutableListOf()
        for (i in splitDate){
            splitDateToInt.add(i.toInt())
        }
        val c1 = Calendar.getInstance()
        c1.set(splitDateToInt[0], splitDateToInt[1] - 1, splitDateToInt[2], 0,0,0) // as MONTH in calender is 0 based.
        val c2 = Calendar.getInstance()
        var diff = c2[Calendar.YEAR] - c1[Calendar.YEAR]
        if (c1[Calendar.MONTH] > c2[Calendar.MONTH] ||
            c1[Calendar.MONTH] == c2[Calendar.MONTH] && c1[Calendar.DATE] > c2[Calendar.DATE]
        ) {
            diff--
        }
        if (diff >= 18){return true } else{return false}
    }

    fun validateNumber(number: String) {
        isNumberFormatValid = !number.contains(Regex(numberReg))
    }

    fun validatePasswordMinLength(password: String) {
        isPasswordMinLengthValid = password.length < 8
    }

    fun validateEmailAll(): Boolean {
        return if (!isEmailFormatValid && email.isNotBlank()) {
            true
        } else {
            false
        }
    }

    fun validatePhoneNumberAll(): Boolean {
        return if (phoneNumber.isNotBlank() && phoneNumber.length == 10 && !isNumberFormatValid) {
            true
        } else {
            false
        }
    }

    fun validatePasswordAll(): Boolean {
        return if (password.isNotBlank() && !isPasswordMinLengthValid) { true } else { false } }

    fun validateDOBAll(selectedDate: String): Boolean {
        return if (selectedDate.isNotBlank() &&
            isUserOver18(convertDobLongToTime(convertDobDateToLong(selectedDate))))
        { true } else { false } }

    fun validateNameAll(): Boolean { return if (name.isNotBlank()) { true } else { false } }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current



    LaunchedEffect(authState.value) {
        when (authState.value) {
            //is AuthState.Authenticated -> navController.navigate(Graph.MAIN)
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sign up now", fontSize = 32.sp, fontFamily = alefFontFamily,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Please fill the details and create account",
            fontSize = 16.sp,
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal,
            color = Grey
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { input ->
                if (input.length <= nameLimit && input.all { !it.isDigit() }) {
                    name = input
                }
                if (name.isNotBlank()) {
                    nameIsEmptyMessage = null
                }
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.size(width = 335.dp, height = 80.dp),
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = "Name", color = Grey, fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            maxLines = 1,
            isError = nameIsEmptyMessage != null,//isNameLengthExceeds &&  isNameContainsNumber
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            supportingText = {
                if (nameIsEmptyMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = nameIsEmptyMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Column {
            val context = LocalContext.current
            val calendar = Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                context,
                R.style.CustomDatePickerDialogTheme,
                { _, year, month, day ->
                    selectedDate = "$day ${getMonthName(month)} $year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis // disables future dates

            OutlinedTextField(
                value = selectedDate,
                onValueChange = {
                    selectedDate = it
                },
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
                readOnly = true,
                modifier = Modifier.width(335.dp)
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
                        text = "Date of Birth", color = Grey, fontSize = 16.sp,
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Normal
                    )
                },
                maxLines = 1,
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select date") },
                isError = userIsNot18Message != null || DOBIsEmptyMessage != null ,
                supportingText = {
                    if (DOBIsEmptyMessage != null){
                        Text(
                            modifier = Modifier.height(35.dp),
                            text = DOBIsEmptyMessage ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (userIsNot18Message != null){
                        Text(
                            modifier = Modifier.height(35.dp),
                            text = userIsNot18Message ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                if (it.length <= emailLimit) {
                    email = it
                }
                // validateLength(email,emailLimit)
                validateEmail(email)
                if (email.isNotBlank()) {
                    emailIsEmptyMessage = null
                }
                if (!isEmailFormatValid) {
                    emailIsInvalidMessage = null
                }
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.width(335.dp),
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = "Email address", color = Grey, fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailIsInvalidMessage != null || emailIsEmptyMessage != null,//&& isEmailLengthExceedsLimit
            supportingText = {
                if (emailIsInvalidMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = emailIsInvalidMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (emailIsEmptyMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = emailIsEmptyMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { input ->
                if (input.length <= phoneNumberLimit && input.all { it.isDigit() }) {
                    phoneNumber = input
                }
                // validateLength(phoneNumber,phoneNumberLimit)
                validateNumber(phoneNumber)
                if (phoneNumber.isNotBlank()) {
                    phoneNumberIsEmptyMessage = null
                }
                if (!isNumberFormatValid) {
                    phoneNumberIsNotValidMessage = null
                }
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.width(335.dp),
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = "Phone number", color = Grey, fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            placeholder = { Text(text = "050 000 0000", color = Color.Gray) },
            // leadingIcon = { Text("05") },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = phoneNumberIsNotValidMessage != null || phoneNumberIsEmptyMessage != null, //&& isPhoneNumberExceedsLimit
            supportingText = {
                if (phoneNumberIsNotValidMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = phoneNumberIsNotValidMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (phoneNumberIsEmptyMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = phoneNumberIsEmptyMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= passwordLimit) {
                    password = it
                }
                validateLength(password, passwordLimit)
                validatePasswordMinLength(password)
                if (!isPasswordMinLengthValid) {
                    passwordMinLengthMessage = null
                }
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.width(335.dp),
            shape = RoundedCornerShape(12),
            label = {
                Text(
                    text = "Password", color = Grey, fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            maxLines = 1,
            isError = passwordMinLengthMessage != null,
            supportingText = {
                Text(
                    modifier = Modifier.height(35.dp),
                    text = passwordMustBeGrayMessage,
                    color = Color.Gray
                )
                if (passwordMinLengthMessage != null) {
                    Text(
                        modifier = Modifier.height(35.dp),
                        text = passwordMinLengthMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedButton(
            onClick = {
                if (name.isBlank()) {
                    nameIsEmptyMessage = "Please enter your name"
                }
                if (email.isBlank() && !isEmailFormatValid) {
                    emailIsEmptyMessage = "Please enter your email"
                }
                if (phoneNumber.isBlank()) {
                    phoneNumberIsEmptyMessage = "Please enter your phone number"
                }
                if (isPasswordMinLengthValid || password.isBlank()) {
                    passwordMinLengthMessage = "Password must be at least 8 characters long"
                }
                if (isEmailFormatValid) {
                    emailIsInvalidMessage = "Please enter a valid email (e.g., name@example.com)"
                }
                if (phoneNumber.isNotBlank() && phoneNumber.length < 10) {
                    phoneNumberIsEmptyMessage = "Please enter a valid number (e.g., 050 000 0000)"
                }
                if (phoneNumber.isNotBlank() && phoneNumber.length == 10 && isNumberFormatValid) {
                    phoneNumberIsNotValidMessage =
                        "Please enter a valid number (e.g., 050 000 0000)"
                }
                if (selectedDate.isBlank()){DOBIsEmptyMessage="Please enter your date of birth"}
                if (selectedDate.isNotBlank()){
                    if (!isUserOver18(convertDobLongToTime(convertDobDateToLong(selectedDate)))){
                        userIsNot18Message="You must be 18 and above"
                    }
                }
                if(selectedDate.isNotBlank()){DOBIsEmptyMessage=null}
                if (selectedDate.isNotBlank()){
                    if (isUserOver18(convertDobLongToTime(convertDobDateToLong(selectedDate)))){
                        userIsNot18Message= null
                    }
                }
                if (validatePasswordAll() && validateEmailAll() && validatePhoneNumberAll()
                    && validateNameAll() && validateDOBAll(selectedDate)) {
                    authViewModel.signup(
                        name = name,
                        dateOfBirth = convertDobLongToTime(convertDobDateToLong(selectedDate)),
                        email = email,
                        phone = phoneNumber,
                        password = password
                    ) { success, error ->
                        if (success) {
                            navController.navigate(Graph.SELECTION) {
                                popUpTo(Graph.AUTHENTICATION) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, enabled = authState.value != AuthState.Loading,
            modifier = Modifier.size(width = 335.dp, height = 56.dp),
            shape = RoundedCornerShape(12),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
        ) {
            Text(
                text = "Sign Up", fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        TextButton(onClick = {
            navController.navigate(AuthScreens.signIn.route)
        }) {
            Text(
                "Already have an account? ", fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal, color = Grey
            )
            Text(
                "Sign in", color = Orange, fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

