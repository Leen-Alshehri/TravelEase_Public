package com.example.travelease.pages

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travelease.AuthState
import com.example.travelease.AuthViewModel
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.alefFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordPage(modifier: Modifier = Modifier,
                       navController: NavController,
                       authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var isEmailFormatValid by remember { mutableStateOf(false) }
    var emailIsInvalidMessage by remember { mutableStateOf<String?>(null) }
    var emailIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var showSendEmailAnimation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val emailReg="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    val emailLimit = 35

    fun validateEmail(email: String){
        isEmailFormatValid= !email.contains(Regex(emailReg))
    }

    fun validateEmailAll():Boolean{
        return if (!isEmailFormatValid && email.isNotBlank()){ true } else{ false }
    }

    LaunchedEffect(authState.value){
        when (authState.value) {
            is AuthState.Success -> {
                Log.d("ResetPassword", "Reset password email sent; navigating to sign in screen")
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    //
                    IconButton(onClick = {
                        navController.popBackStack()}
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
            )
        },
        containerColor = Color.White
    ){values ->

    Column(
        modifier = modifier.fillMaxSize().padding(values),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Forgot Password", fontSize = 32.sp, fontFamily = alefFontFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 15.dp))
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Enter your account's email to reset your password", fontSize = 16.sp,
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal, color = Grey,
            textAlign = TextAlign.Center, modifier = Modifier.padding(start = 20.dp, end = 20.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                if(it.length <= emailLimit){
                    email = it
                }
                validateEmail(email)
                if (email.isNotBlank()){emailIsEmptyMessage = null}
                if (!isEmailFormatValid){emailIsInvalidMessage = null}
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.size(width = 335.dp, height = 80.dp),
            shape = RoundedCornerShape(12),
            label = { Text(text = "Email address" , color = Grey,fontSize = 16.sp,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal) },
            maxLines = 1,
            isError = emailIsInvalidMessage != null || emailIsEmptyMessage != null,
            supportingText = {
                if (emailIsInvalidMessage != null){
                    Text( modifier = Modifier.fillMaxSize(),
                        text = emailIsInvalidMessage ?: "",
                        color= MaterialTheme.colorScheme.error)
                }
                if(emailIsEmptyMessage != null){
                    Text( modifier = Modifier.fillMaxSize(),
                        text = emailIsEmptyMessage ?: "",
                        color= MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(30.dp))

        if (showSendEmailAnimation){
            BasicAlertDialog(
                onDismissRequest = {showSendEmailAnimation = false},
                content = {
                    Column(
                        modifier = Modifier.height(400.dp).width(310.dp)
                            .clip(shape = RoundedCornerShape(20.dp))
                            .background(color = Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LottieAnimation(
                            modifier = Modifier.size(250.dp), // Success animation size
                            animationFile = "email_sent.json"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Check your email",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "We have sent a password recovery instruction to your email",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }

        OutlinedButton(onClick =  {
            if (email.isBlank() && !isEmailFormatValid){emailIsEmptyMessage="Please enter your email"}
            if (isEmailFormatValid){
                emailIsInvalidMessage="Please enter a valid email (e.g., name@example.com)"
            }
            if (validateEmailAll()){
                authViewModel.resetPasswordUsingEmail(email,{showSendEmailAnimation = it})
                coroutineScope.launch {
                    kotlinx.coroutines.delay(2500)
                    showSendEmailAnimation = false
                }
            }
        }, enabled = authState.value != AuthState.Loading,
            modifier = Modifier.size(width = 335.dp, height = 56.dp),
            shape = RoundedCornerShape(12),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
        ) {
            Text(text = "Reset Password", fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color.White)
        }
    }
}
}