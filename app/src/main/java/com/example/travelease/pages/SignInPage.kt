package com.example.travelease.pages

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.travelease.AuthState
import com.example.travelease.AuthViewModel
import com.example.travelease.navigation.AuthScreens
import com.example.travelease.navigation.Graph
import com.example.travelease.ui.theme.DMSansFontFamily
import com.example.travelease.ui.theme.Grey
import com.example.travelease.ui.theme.OceanBlue
import com.example.travelease.ui.theme.Orange
import com.example.travelease.ui.theme.alefFontFamily

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SignInPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isPasswordLengthExceedsLimit by remember { mutableStateOf(false) }
    var isEmailFormatValid by remember { mutableStateOf(false) }
    var isPasswordMinLengthValid by remember{ mutableStateOf(false) }
    var emailIsInvalidMessage by remember { mutableStateOf<String?>(null) }
    var emailIsEmptyMessage by remember { mutableStateOf<String?>(null) }
    var passwordMinLengthMessage by remember { mutableStateOf<String?>(null) }

    val emailLimit = 35
    val passwordLimit =15
    val emailReg="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    fun validateLength(text: String, limit:Int){
        if (limit==passwordLimit){
            isPasswordLengthExceedsLimit= text.length > limit
        }
    }

    fun validateEmail(email: String){
        isEmailFormatValid= !email.contains(Regex(emailReg))
    }

    fun validatePasswordMinLength(password: String){
        isPasswordMinLengthValid= password.length <8
    }

    fun validateEmailAll():Boolean{
        return if (!isEmailFormatValid && email.isNotBlank()){ true } else{ false }
    }

    fun validatePasswordAll():Boolean{
        return if (password.isNotBlank() && !isPasswordMinLengthValid){true}
        else {false}
    }

//    LaunchedEffect(authState.value) {
//        when (authState.value) {
//            is AuthState.Authenticated -> navController.navigate(Graph.MAIN)
//            is AuthState.Error -> Toast.makeText(
//                context,
//                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
//            ).show()
//
//            else -> Unit
//        }
//    }
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> {
                Log.d("SignUp", "User authenticated! Navigating to main screen")
                navController.navigate(Graph.MAIN) {
                    popUpTo(Graph.AUTHENTICATION) { inclusive = true }  // Clears Auth screens from backstack
                }
            }
            is AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }


    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign in now", fontSize = 32.sp, fontFamily = alefFontFamily,
            fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Please sign in to continue", fontSize = 16.sp, fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal, color = Grey)

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
            modifier = Modifier.width(335.dp),
            shape = RoundedCornerShape(12),
            label = { Text(text = "Email address" , color = Grey,fontSize = 16.sp,
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal) },
            maxLines = 1,
            isError = emailIsInvalidMessage != null || emailIsEmptyMessage != null,
            supportingText = {
                if (emailIsInvalidMessage != null){
                    Text( modifier = Modifier.height(35.dp),
                        text = emailIsInvalidMessage ?: "",
                        color= MaterialTheme.colorScheme.error)
                }
                if(emailIsEmptyMessage != null){
                    Text( modifier = Modifier.height(35.dp),
                        text = emailIsEmptyMessage ?: "",
                        color= MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if(it.length <= passwordLimit){
                    password = it
                }
                validateLength(password,passwordLimit)
                validatePasswordMinLength(password)
                if (!isPasswordMinLengthValid){passwordMinLengthMessage=null}
            },
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Grey),
            modifier = Modifier.width(335.dp),
            shape = RoundedCornerShape(12),
            label = {
                Text(text = "Password", color = Grey,fontSize = 16.sp,
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal)
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
                if(passwordMinLengthMessage != null){
                    Text( modifier = Modifier.height(35.dp),
                        text = passwordMinLengthMessage ?: "",
                        color= MaterialTheme.colorScheme.error)
                }
            }
        )
        Text(
            "Forgot Password? ", fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Normal, color = Orange,
            modifier = Modifier.align(Alignment.End).padding(top = 5.dp, end = 20.dp)
                .clickable {
                    navController.navigate(AuthScreens.forgotPassword.route){
                        popUpTo(navController.graph.findStartDestination().id)
                    }
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedButton(onClick =  {
            if (email.isBlank() && !isEmailFormatValid){emailIsEmptyMessage="Please enter your email"}
            if (isPasswordMinLengthValid || password.isBlank()){
                passwordMinLengthMessage = "Password must be at least 8 characters long"
            }
            if (isEmailFormatValid){
                emailIsInvalidMessage="Please enter a valid email (e.g., name@example.com)"
            }
            if (validatePasswordAll() && validateEmailAll()  ){
                authViewModel.signin(email,password)
            }
        }, enabled = authState.value != AuthState.Loading,
            modifier = Modifier.size(width = 335.dp, height = 56.dp),
            shape = RoundedCornerShape(12),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
        ) {
            Text(text = "Sign In", fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(25.dp))

        TextButton(onClick =  {
            navController.navigate(AuthScreens.signUp.route)
        }) {
            Text("Don't have an account? ", fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal , color = Grey)
            Text("Sign up", color = Orange, fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal)
        }
    }
}

