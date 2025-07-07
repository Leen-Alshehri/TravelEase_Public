package com.example.travelease.pages

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.travelease.AuthViewModel
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.entities.Traveler
import com.example.travelease.R
import com.example.travelease.navigation.Graph
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.platform.LocalContext as LocalContext1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier = Modifier,
                navController: NavController,
                authViewModel: AuthViewModel,
                dbViewModel: dbViewModel,
                rootNavController: NavController) {


    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var traveler by remember { mutableStateOf<Traveler?>(null) }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(travelerId) {
        travelerId?.let {
            dbViewModel.getTraveler(it) { fetchedTraveler ->
                traveler = fetchedTraveler
                //imageUri = null
                fetchedTraveler?.let {
                    name = it.name
                    phoneNumber = it.phoneNumber
                    email = it.email
                    dob = it.dateOfBirth
                }
            }
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var isEmailFormatValid by remember { mutableStateOf(false) }
    var isNumberFormatValid by remember { mutableStateOf(false) }
    var nameIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var isOver18ErrorMessage by remember { mutableStateOf<String?>(null) }
    var phoneNumberIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var phoneNumberIsNotValidMessage by remember { mutableStateOf<String?>(null) }
    var emailIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var emailIsNotValidMessage by remember { mutableStateOf<String?>(null) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var navigateToSignIn by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext1.current
    val calendar = Calendar.getInstance()

    val emailReg = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    val numberReg = "^(?![123456789])[0-9](?![123467890])[0-9]"


    fun isUserOver18(date: String): Boolean {
        var newDate = date
        var splitDate = newDate.split('-')
        val splitDateToInt: MutableList<Int> = mutableListOf()
        for (i in splitDate) {
            splitDateToInt.add(i.toInt())
        }
        val c1 = Calendar.getInstance()
        c1.set(
            splitDateToInt[0], splitDateToInt[1] - 1,
            splitDateToInt[2], 0, 0, 0
        ) // as MONTH in calender is 0 based.
        val c2 = Calendar.getInstance()
        var diff = c2[Calendar.YEAR] - c1[Calendar.YEAR]
        if (c1[Calendar.MONTH] > c2[Calendar.MONTH] ||
            c1[Calendar.MONTH] == c2[Calendar.MONTH] && c1[Calendar.DATE] > c2[Calendar.DATE]
        ) {
            diff--
        }
        if (diff >= 18) {
            return true
        } else {
            return false
        }
    }


    fun validateEmail(email: String) {
        isEmailFormatValid = !email.contains(Regex(emailReg))
    }

    fun validateNumber(number: String) {
        isNumberFormatValid = !number.contains(Regex(numberReg))
    }

    fun validatePhoneNumberAll(): Boolean {
        return if (phoneNumber.isNotBlank() && phoneNumber.length == 10 && !isNumberFormatValid) {
            true
        } else {
            false
        }
    }

    fun validateEmailAll(): Boolean {
        return if (!isEmailFormatValid && email.isNotBlank()) {
            true
        } else {
            false
        }
    }

    fun validateDOBAll(): Boolean {
        return if (dob.isNotBlank() && isUserOver18(dob)) {
            true
        } else {
            false
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            imageUri = uri
        }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Account", fontFamily = alefFontFamily)
                },
                navigationIcon = {
                    //check if the user goes back that there will be no updates in db
                    IconButton(onClick = {
                        navController.navigate(Graph.MAIN)
                        {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    Text(
                        text = if (isEditing) "Done" else "Edit",
                        color = Orange,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            if (isEditing) {
                                if (name.isBlank() || !validateDOBAll()
                                    || !validatePhoneNumberAll() || !validateEmailAll()
                                ) {
                                    nameIsEmptyMessage =
                                        if (name.isBlank()) "This field can not be empty"
                                        else null
                                    isOver18ErrorMessage =
                                        if (!isUserOver18(dob)) "You must be 18 and above"
                                        else null
                                    if (phoneNumber.isBlank()) {
                                        phoneNumberIsEmptyMessage = "This field can not be empty"
                                    }
                                    if (phoneNumber.isNotBlank() && phoneNumber.length < 10) {
                                        phoneNumberIsEmptyMessage =
                                            "Enter a valid number (e.g., 050 000 0000)"
                                    }
                                    if (phoneNumber.isNotBlank() && phoneNumber.length == 10 &&
                                        isNumberFormatValid
                                    ) {
                                        phoneNumberIsNotValidMessage =
                                            "Please enter a valid number (e.g., 050 000 0000)"
                                    }
                                    if (email.isBlank() && !isEmailFormatValid) {
                                        emailIsEmptyMessage = "Please enter your email"
                                    }
                                    if (isEmailFormatValid) {
                                        emailIsNotValidMessage =
                                            "Please enter a valid email (e.g., name@example.com)"
                                    }
                                } else {
                                    isOver18ErrorMessage = null
                                    nameIsEmptyMessage = null
                                    phoneNumberIsEmptyMessage = null
                                    phoneNumberIsNotValidMessage = null
                                    emailIsEmptyMessage = null
                                    emailIsNotValidMessage = null

                                    val base64Image =
                                        imageUri?.let { uriToBase64IfSmallEnough(context, it) }
                                    val updatedTraveler = travelerId?.let {
                                        Traveler(
                                            travelerId = it,
                                            name = name,
                                            phoneNumber = phoneNumber,
                                            email = email,
                                            dateOfBirth = dob,
                                            userimage = base64Image
                                        )
                                    }
                                    if (updatedTraveler != null) {
                                        dbViewModel.updateTraveler(updatedTraveler)

                                        dbViewModel.getTraveler(travelerId) { fetchedTraveler ->
                                            traveler = fetchedTraveler
                                        }
                                        Toast.makeText(context, "Information Updated successfully", Toast.LENGTH_SHORT).show()
                                        imageUri = null
                                    }

                                    isEditing = false
                                }
                            } else {
                                isEditing = true
                            }
                        }
                    )
                },
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
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0C3D8D))
                ) {
                    Text(text = "Sign Out", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    ) { values ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(values),
          //  verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageUri != null && isEditing) {
                    // Show selected image only during editing
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    )
                } else if (!isEditing && traveler?.userimage != null) {
                    // Show image from Firestore when not editing
                    val imageBytes = android.util.Base64.decode(
                        traveler!!.userimage,
                        android.util.Base64.DEFAULT
                    )
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    )
                } else {
                    // Fallback default icon
                    Image(
                        painter = painterResource(id = R.drawable.usericon),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier
                            .height(90.dp)
                            .width(90.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Change Profile Picture",
                    color = Orange,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 150.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            Column(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                LazyColumn {
                    item {
                        Text(
                            "Name", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        NameInputField(name, isEditing, nameIsEmptyMessage,
                            { nameIsEmptyMessage = it }) { name = it }

                        Text(
                            text = "Date of Birth", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        DoBInputField(value = dob,
                            isEditable = isEditing,
                            isDate = true,
                            isOver18ErrorMessage = isOver18ErrorMessage,
                            onValueChange = { dob = it })

                        Text(
                            text = "Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        PhoneNumberInputField(phoneNumber, isEditing,
                            emptyErrorMessage = phoneNumberIsEmptyMessage,
                            invalidErrorMessage = phoneNumberIsNotValidMessage,
                            isNumberFormatValid = isEmailFormatValid,
                            setEmptyErrorMessage = { phoneNumberIsEmptyMessage = it },
                            setInvalidErrorMessage = { phoneNumberIsNotValidMessage = it },
                            validateNumber = { validateNumber(phoneNumber) })
                        { phoneNumber = it }

                        Text(
                            text = "Email Address", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        EmailAddressInputField(value = email, isEditable = isEditing,
                            emptyErrorMessage = emailIsEmptyMessage,
                            invalidErrorMessage = emailIsNotValidMessage,
                            isEmailFormatValid = isEmailFormatValid,
                            setErrorMessage = { emailIsEmptyMessage = it },
                            setInvalidErrorMessage = { emailIsNotValidMessage = it },
                            validateEmail = { validateEmail(email) })
                        { email = it }


                        // Confirm Button

                    }
                }
            }




            // Sign Out Confirmation Dialog
            if (showLogoutDialog) {
                AlertDialog(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White,
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(
                            text = "Sign Out",
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
                                text = "Are you sure you want to sign out?",
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
                                onClick = { showLogoutDialog = false },
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
                                onClick = {
                                    Log.d("Profile", "Sign out button clicked")
                                    authViewModel.signout()  // Perform Firebase sign-out
                                    showLogoutDialog = false
                                    coroutineScope.launch {
                                        delay(300)  // Small delay before navigating
                                        rootNavController.navigate(Graph.AUTHENTICATION) {
                                            popUpTo(Graph.ROOT) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Yes",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NameInputField(value: String,
                   isEditable: Boolean,
                   errorMessage: String?,
                   setErrorMessage: (String?) -> Unit,
                   onValueChange: (String) -> Unit

) {
    val nameLimit = 50
    OutlinedTextField(
        value = value,
        onValueChange = {input ->
            if (input.length <= nameLimit && input.all { !it.isDigit() }) {
                onValueChange(input)
            }
            if (value.isNotBlank()){setErrorMessage(null)}
        },
        readOnly = !isEditable ,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        isError = errorMessage != null,
        supportingText = {
            if (errorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun DoBInputField(value: String,
                  isEditable: Boolean,
                  isDate: Boolean,
                  isOver18ErrorMessage: String?,
                  onValueChange: (String) -> Unit){

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        R.style.CustomDatePickerDialogTheme,
        { _, year, month, day ->
            val formattedDate = String.format(Locale.US,
                "%04d-%02d-%02d", year, month + 1, day)
            onValueChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.maxDate = calendar.timeInMillis

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(value) },
        readOnly = !isEditable || isDate ,
        modifier = Modifier.fillMaxWidth().padding(8.dp).then(
            if (isEditable){ Modifier.pointerInput(value) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                    if (upEvent != null) {
                        datePickerDialog.show()
                    }
                }
            }
            }
            else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        isError = isOver18ErrorMessage != null,
        supportingText = {
            if (isOver18ErrorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = isOver18ErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun PhoneNumberInputField(value: String,
                          isEditable: Boolean,
                          emptyErrorMessage: String?,
                          invalidErrorMessage: String?,
                          isNumberFormatValid: Boolean,
                          setEmptyErrorMessage: (String?) -> Unit,
                          validateNumber: (String) -> Unit,
                          setInvalidErrorMessage: (String?) -> Unit,
                          onValueChange: (String) -> Unit) {
    val phoneNumberLimit = 10
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.length <= phoneNumberLimit && input.all { it.isDigit() }) {
                onValueChange(input)
            }
            validateNumber(value)
            if (value.isNotBlank()){
                setEmptyErrorMessage(null)
            }
            if (!isNumberFormatValid) {
                setInvalidErrorMessage(null)
            }
        },
        readOnly = !isEditable ,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        isError = emptyErrorMessage != null || invalidErrorMessage != null,
        supportingText = {
            if (emptyErrorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = emptyErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (invalidErrorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = invalidErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun EmailAddressInputField(value: String,
                           isEditable: Boolean,
                           emptyErrorMessage: String?,
                           invalidErrorMessage: String?,
                           isEmailFormatValid: Boolean,
                           setErrorMessage: (String?) -> Unit,
                           setInvalidErrorMessage: (String?) -> Unit,
                           validateEmail: (String) -> Unit,
                           onValueChange: (String) -> Unit) {
    val emailLimit = 35

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= emailLimit) {
                onValueChange(it)
            }
            validateEmail(value)
            if (value.isNotBlank()) {
                setErrorMessage(null)
            }
            if (!isEmailFormatValid) {
                setInvalidErrorMessage(null)
            }
        },
        readOnly = !isEditable,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        isError = emptyErrorMessage != null || invalidErrorMessage != null,
        supportingText = {
            if (emptyErrorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = emptyErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (invalidErrorMessage != null){
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = invalidErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )

}

fun uriToBase64IfSmallEnough(context: Context, uri: Uri): String? {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes()
    inputStream?.close()
    return if (bytes != null && bytes.size <= 1_000_000) {
        android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
    } else {
        null // too large than 1MB "The allowed size"
    }
}
