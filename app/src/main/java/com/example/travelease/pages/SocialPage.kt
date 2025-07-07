package com.example.travelease.pages

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavController
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.R
import com.example.travelease.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.example.travelease.firebaseDB.entities.Post
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import com.example.travelease.fromToList
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.travelease.ui.theme.DMSansFontFamily
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Base64
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.ui.layout.ContentScale
import com.example.travelease.ui.theme.alefFontFamily
import java.io.ByteArrayOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    dbViewModel: dbViewModel,
) {
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var recentPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var myPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Recent, 1 = My Posts
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCity, selectedTab) {
        isLoading = true
        if (travelerId != null) {
            if (selectedTab == 0) {
                dbViewModel.getAllPosts(travelerId, selectedCity) { result ->
                    recentPosts = result.sortedByDescending { post ->
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(post.publicationDate)
                    }
                    isLoading = false
                }
            } else {
                dbViewModel.getPostsByTraveler(travelerId) { result ->
                    myPosts = result.sortedByDescending { post ->
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(post.publicationDate)
                    }
                    isLoading = false
                }
            }
        }
    }
    val posts = if (selectedTab == 0) recentPosts else myPosts

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Explore",
                        fontSize = 20.sp,
                        fontFamily = alefFontFamily
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Tabs "My posts" "Recent"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.Red else Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    )

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "My posts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.Red else Color.Gray,
                            modifier = Modifier.clickable { selectedTab = 1 }
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        IconButton(onClick = { navController.navigate(Screens.CreatePost.route) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Post",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = Color.LightGray,
                    thickness = 0.8.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 0) {
                    DropdownMenuExample(selectedCity) { city ->
                        selectedCity = city
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        posts.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "No posts yet...",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ready to inspire?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White)
                            ) {
                                items(posts) { post ->
                                    Column {
                                        PostItem(
                                            post = post,
                                            travelerId = travelerId ?: "",
                                            navController = navController,
                                            dbViewModel = dbViewModel,
                                            onDeletePost = {
                                                dbViewModel.deletePost(post.travelerId, post.postId)
                                                if (selectedTab == 0) {
                                                    recentPosts =
                                                        recentPosts.filter { it.postId != post.postId }
                                                } else {
                                                    myPosts =
                                                        myPosts.filter { it.postId != post.postId }
                                                }
                                            }
                                        )
                                        Divider(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            color = Color(0xFFE0E0E0),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}





@Composable
fun DropdownMenuExample(selectedCity: String?, onCitySelected: (String?) -> Unit) {
    val cities = listOf("All") + fromToList // "All" option to display all posts
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        Text(
            text = selectedCity ?: "Filter by City",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(16.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(160.dp).height(200.dp),
            containerColor = Color.White
        ) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(
                        text= city,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = DMSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold)
                           },
                    onClick = {
                        expanded = false
                        onCitySelected(if (city == "All") null else city)
                    }
                )
            }
        }
    }
}


@Composable
fun PostItem(
    post: Post,
    travelerId: String,
    navController: NavController,
    dbViewModel: dbViewModel,
    showDeleteIcon: Boolean = false,
    onDeletePost: (() -> Unit)? = null
) {
    var travelerName by remember { mutableStateOf("Loading...") }
    var liked by remember { mutableStateOf(post.likedBy.contains(travelerId)) }
    var likesCount by remember { mutableStateOf(post.totalLikes) }
    var commentsCount by remember { mutableStateOf(post.totalComments) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userImageBase64 by remember { mutableStateOf<String?>(null) }
    var showDeleteSuccess by remember { mutableStateOf(false) }



    LaunchedEffect(post.travelerId) {
        dbViewModel.getNameByID(post.travelerId) { name ->
            travelerName = name ?: "Unknown Traveler"
        }
    }

    LaunchedEffect(post.postId) {
        liked = post.likedBy.contains(travelerId)
        likesCount = post.totalLikes
    }

    LaunchedEffect(post.postId) {
        dbViewModel.getPostOwnerId(post.postId) { ownerId ->
            ownerId?.let {
                dbViewModel.getTraveler(it) { traveler ->
                    userImageBase64 = traveler?.userimage
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (userImageBase64 != null) {
                val imageBytes = Base64.decode(userImageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.usericon),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = travelerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = post.publicationDate.substring(0, 16), fontSize = 12.sp, color = Color.Gray)
                Text(text = post.city ?: "Unknown Location", fontSize = 12.sp, color = Color.Gray)
            }

            if (post.travelerId == travelerId && onDeletePost != null) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Outlined.Backspace,
                    contentDescription = "Delete Post",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showDeleteDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.postText, fontSize = 14.sp)

        post.imageRes?.takeIf { it.isNotBlank() }?.let { base64Image ->
            val bitmap = remember(base64Image) {
                try {
                    val imageBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: Exception) {
                    Log.e("PostItem", "Failed to decode base64 image: ${e.message}")
                    null
                }
            }

            bitmap?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = if (liked) R.drawable.full_heart else R.drawable.empty_heart),
                contentDescription = "Like Button",
                tint = if (liked) Color.Red else Color.Gray,
                modifier = Modifier.size(24.dp).clickable {
                    liked = !liked
                    if (liked) {
                        dbViewModel.likePost(travelerId, post.travelerId, post.postId)
                        likesCount++
                    } else {
                        dbViewModel.unlikePost(travelerId, post.travelerId, post.postId)
                        likesCount--
                    }
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$likesCount")

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                painter = painterResource(id = R.drawable.comment_icon),
                contentDescription = "Comment Button",
                modifier = Modifier.size(24.dp).clickable {
                    navController.navigate("${Screens.Comments.route}/${post.postId}")
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$commentsCount", fontSize = 14.sp)
        }
    }
    if (showDeleteDialog) {
        DeleteTripConfirmationDialogs(
            onConfirm = {
                //onDeletePost?.invoke()
                showDeleteDialog = false
                showDeleteSuccess =true
            },

            onDismiss = { showDeleteDialog = false }
        )
    }
    if (showDeleteSuccess) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {},
            text = {
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
                        text = "Deleted Successfully!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {}
        )
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2500)
            onDeletePost?.invoke()
            showDeleteSuccess = false
        }
    }

}

@Composable
fun DeleteTripConfirmationDialogs(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Delete Post?",
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
                androidx.compose.material.Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This will delete Post permanently.",
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
                androidx.compose.material.OutlinedButton(
                    onClick = onDismiss,
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
                    onClick = onConfirm,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    )
}

//Create post screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, dbViewModel: dbViewModel) {
    var postText by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var expanded by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }


    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        imageErrorMessage = null
    }

    val activity = context as? Activity

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), 1001)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.Start
    ) {
        // Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Add Picture", fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        // Add Picture Icon without border
        Box(
            modifier = Modifier
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.add_post_icon),
                contentDescription = "Add Picture",
                modifier = Modifier.size(70.dp)
            )
        }


        Spacer(modifier = Modifier.height(8.dp))
        // Error message if image exceeds 1MB
        imageErrorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 12.sp
            )
        }


        Spacer(modifier = Modifier.height(12.dp))
        selectedImageUri?.let { uri ->
            Image(
                painter = rememberImagePainter(uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No picture larger than 1MB is allowed.",
            fontSize = 12.sp,
            color = Color.Gray
        )

        // TextField
        TextField(
            value = postText,
            onValueChange = { postText = it },
            placeholder = { Text("Type something here..") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Select City Button
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .width(150.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                ) {
                    Text(
                        text = selectedCity ?: "Select a City",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(150.dp)
                        .height(200.dp)
                        .align(Alignment.CenterHorizontally),
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
                                expanded = false
                                selectedCity = city
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Post Button
        //Button(
        //onClick = {
//                if (!postText.isBlank() && selectedCity != null) {
//                    val post = Post(
//                        postId = UUID.randomUUID().toString(),
//                        travelerId = travelerId,
//                        postText = postText,
//                        city = selectedCity!!,
//                        imageRes = selectedImageUri?.toString(),
//                        totalComments = 0,
//                        totalLikes = 0,
//                        likedBy = emptyList(),
//                        publicationDate = getCurrentDateTime()
//                    )
//                    dbViewModel.addPost(travelerId, post)
//                    navController.popBackStack() // Navigate back to the SocialPage
//                }
        val postId = UUID.randomUUID().toString()
        if (!showSuccessAnimation) {
            Button(
                onClick = {
                    if (!postText.isBlank() && selectedCity != null) {
                        isUploading = true

                        val base64Image = selectedImageUri?.let { uriToBase64(context, it) }

                        if (selectedImageUri != null && base64Image == null) {
                            imageErrorMessage = "Image too large. Please select an image under 1MB."
                            errorMessage = null
                            isUploading = false
                            return@Button
                        }

                        val post = Post(
                            postId = UUID.randomUUID().toString(),
                            travelerId = travelerId,
                            postText = postText,
                            city = selectedCity!!,
                            imageRes = base64Image,
                            totalComments = 0,
                            totalLikes = 0,
                            likedBy = emptyList(),
                            publicationDate = getCurrentDateTime()
                        )

                        dbViewModel.addPost(travelerId, post)

                        showSuccessAnimation = true
                        isUploading = false
                    } else {
                        errorMessage = "Please write something and select a city."
                        imageErrorMessage = null
                    }
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(45.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = Color(0xFF0C3D8D)
                ),
                enabled = !isUploading
            ) {
                Text(
                    text = if (isUploading) "Posting..." else "Post",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (showSuccessAnimation) {
            AlertDialog(
                onDismissRequest = {},
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                title = {},
                text = {
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
                            text = "Posted Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {} // No buttons here
            )

            // Navigate back
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2500)
                navController.popBackStack()
            }
        }



    }
}




@Composable
fun rememberImagePainter(uri: Uri): Painter {
    return rememberAsyncImagePainter(uri)
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()
        if (bytes.size > 900_000) return null
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


