package com.example.travelease.pages
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.R
import com.example.travelease.navigation.Graph
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SelectionPage(
    modifier: Modifier = Modifier,
    parentNavController: NavHostController,
    viewModel: dbViewModel
) {
    val localNavController = rememberNavController()
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val preferences = remember { mutableStateMapOf<String, String>() }
    val coroutineScope = rememberCoroutineScope()
    val navigateNow = remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(navController = localNavController, startDestination = "destination_screen") {
            composable("destination_screen") {
                DestinationScreen(localNavController) { selected ->
                    selected?.let { preferences["Destination"] = it }
                    localNavController.navigate("season_screen")
                }
            }
            composable("season_screen") {
                SeasonScreen(localNavController) { selected ->
                    selected?.let { preferences["Season"] = it }
                    localNavController.navigate("environment_screen")
                }
            }
            composable("environment_screen") {
                val shouldNavigate = remember { mutableStateOf(false) }

                EnvironmentScreen(localNavController) { selected ->
                    selected?.let { preferences["Environment"] = it }

                    viewModel.saveTravelerPreferences(travelerId, preferences,
                        onSuccess = {
                            Log.d("FIRESTORE", "onSuccess: triggering navigation")
                            shouldNavigate.value = true
                        },
                        onFailure = {
                            Log.e("FIRESTORE", "Save failed: $it")
                        }
                    )
                }
                LaunchedEffect(shouldNavigate.value) {
                    if (shouldNavigate.value) {
                        Log.d("NAV", "Navigating to Home...")
                        parentNavController.navigate(Graph.MAIN) {
                            popUpTo(Graph.SELECTION) { inclusive = true }
                            launchSingleTop = true
                        }
                        shouldNavigate.value = false
                    }
                }
            }

        }
    }
}





@Composable
fun DestinationScreen(navController: NavController, onNext: (String?) -> Unit) {
    SelectionScreen(
        title = "Where is your dream vacation to go?",
        options = listOf("London", "Paris", "Neom", "Istanbul"),
        images = listOf(R.drawable.img, R.drawable.img_1, R.drawable.img_2, R.drawable.img_3),
        buttonText = "Next",
        onNextClick = onNext
    )
}

@Composable
fun SeasonScreen(navController: NavController, onNext: (String?) -> Unit) {
    SelectionScreen(
        title = "Which season do you prefer for traveling?",
        options = listOf("Summer", "Spring", "Fall", "Winter"),
        images = listOf(R.drawable.img_4, R.drawable.img_5, R.drawable.img_6, R.drawable.img_7),
        buttonText = "Next",
        onNextClick = onNext
    )
}

@Composable
fun EnvironmentScreen(navController: NavController, onNext: (String?) -> Unit) {
    SelectionScreen(
        title = "What type of environment do you enjoy most?",
        options = listOf("Historical Sites", "Beach", "Snowy", "Cities"),
        images = listOf(R.drawable.img_8, R.drawable.img_9, R.drawable.img_10, R.drawable.img_11),
        buttonText = "Get Started",
        onNextClick = onNext
    )
}



@Composable
fun SelectionScreen(
    title: String,
    options: List<String>,
    images: List<Int>,
    buttonText: String,
    onNextClick: (String?) -> Unit
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 130.dp),
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (i in options.indices step 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectionImage(
                        label = options[i],
                        imageRes = images[i],
                        isSelected = selectedIndex == i,
                        onClick = { selectedIndex = i }
                    )
                    if (i + 1 < options.size) {
                        SelectionImage(
                            label = options[i + 1],
                            imageRes = images[i + 1],
                            isSelected = selectedIndex == i + 1,
                            onClick = { selectedIndex = i + 1 }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(170.dp))
        Button(
            onClick = { onNextClick(selectedIndex?.let { options[it] }) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
                //.padding(top = 60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D))
        ) {
            Text(text = buttonText, fontSize = 16.sp, color = Color.White)
        }
    }
}



@Composable
fun SelectionImage(label: String, imageRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(180.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )
        Icon(
            painter = painterResource(id = if (isSelected) R.drawable.img_13 else R.drawable.img_12),
            contentDescription = if (isSelected) "Selected" else "Unselected",
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(30.dp)
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
