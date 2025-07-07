package com.example.travelease

import android.content.Context
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.travelease.weatherNotifications.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AuthViewModel (private val context: Context) : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val weatherRep = WeatherRepository(context) //Initializes a weather repository


    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _authState.postValue(if (user != null) AuthState.Authenticated else AuthState.Unauthenticated)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun signin(email : String, password : String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated

                    CoroutineScope(Dispatchers.IO).launch { //Fetches weather immediately on a background thread.
                        weatherRep.fetchWeatherAndNotify()
                    }

                    weatherRep.startWeatherNotifications() //set periodic 15min notifications
                }else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun signup(name: String, dateOfBirth: String, email: String, phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Email or password can't be empty")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid  // Firebase UID

                    if (userId != null) {
                        val traveler = hashMapOf(
                            "travelerId" to userId,
                            "name" to name,
                            "dateOfBirth" to dateOfBirth,
                            "email" to email,
                            "phoneNumber" to phone
                        )

                        // Save user data in Firestore
                        db.collection("Travelers").document(userId)
                            .set(traveler)
                            .addOnSuccessListener {
                                Log.d("Firestore", "User saved in Firestore!")
                                onResult(true, null)

                                weatherRep.startWeatherNotifications()
                            }

                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error saving user", e)
                                onResult(false, e.message)
                            }
                    } else {
                        onResult(false, "Failed to retrieve user ID")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun resetPasswordUsingEmail(email: String, setAnimation: (Boolean)-> Unit){
        if(email.isEmpty()){
            _authState.value = AuthState.Error("Email can't be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                _authState.value = AuthState.Success("Reset password email is sent successfully")
                setAnimation(true)
            }else{
                _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
            }
        }
    }



    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }


}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String): AuthState()
}


class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
