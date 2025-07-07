package com.example.travelease

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.travelease.activityApi.ActivitiesViewModel
import com.example.travelease.accommodationsApi.AccommodationViewModel
import com.example.travelease.flightsApi.MainViewModel
import com.example.travelease.firebaseDB.dbRepository
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.firebaseDB.dbViewModelFactory
import com.example.travelease.recommenderSystem.RecommendationViewModel
import com.example.travelease.navigation.RootNavigationGraph
import com.example.travelease.ui.theme.TravelEaseTheme
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.example.travelease.weatherNotifications.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        

        val repository = dbRepository()
        val travelViewModel: dbViewModel by viewModels { dbViewModelFactory(repository) }
        val authViewModel: AuthViewModel by viewModels{
            AuthViewModelFactory(this) // 'this' is the Context
        }
        val mainViewModel: MainViewModel by viewModels()
        val accommodationViewModel: AccommodationViewModel by viewModels()
        val activitiesViewModel: ActivitiesViewModel by viewModels()
        val recommendationViewModel: RecommendationViewModel by viewModels()

        if (FirebaseAuth.getInstance().currentUser != null) {
            val weatherRepo = WeatherRepository(this)

            CoroutineScope(Dispatchers.IO).launch {
                weatherRepo.fetchWeatherAndNotify()  //fetches weather and shows notification
            }

            weatherRepo.startWeatherNotifications()  // sets up periodic 15 min background notifications
        }

        setContent {
            TravelEaseTheme {
                RootNavigationGraph(
                    Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel,
                    accommodationViewModel = accommodationViewModel,
                    activitiesViewModel = activitiesViewModel,
                    travelViewModel = travelViewModel,
                    recommendationViewModel = recommendationViewModel
                )
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Notification permission granted")
            } else {
                Log.d("Permission", "Notification permission denied")
            }
        }
    }
}
