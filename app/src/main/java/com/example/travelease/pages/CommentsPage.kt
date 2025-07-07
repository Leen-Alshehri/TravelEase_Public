package com.example.travelease.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.UUID
import com.example.travelease.firebaseDB.dbViewModel
import com.example.travelease.R
import com.google.firebase.auth.FirebaseAuth
import com.example.travelease.firebaseDB.entities.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsPage(
    navController: NavController,
    username: String,
    postId: String,
    dbViewModel: dbViewModel
) {
    val travelerId = FirebaseAuth.getInstance().currentUser?.uid
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<Comment?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(postId) {
        isLoading = true
        dbViewModel.getCommentsByPost(postId) { fetchedComments ->
            comments = fetchedComments
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp).clickable {
                        navController.popBackStack()
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (comments.isEmpty()) {
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
                                text = "No comments yet...",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Yours can be the first!",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                travelerId = travelerId ?: "",
                                dbViewModel = dbViewModel,
                                onDeleteClicked = {
                                    commentToDelete = comment
                                    showDeleteConfirmation = true
                                }
                            )
                        }
                    }
                }

            }


            // Bottom Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
                    .offset(y = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f) ,
                    shape = RoundedCornerShape(25.dp),
                    placeholder = { Text("Type your comment...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            val newComment = Comment(
                                commentId = UUID.randomUUID().toString(),
                                postId = postId,
                                travelerId = travelerId ?: "",
                                username = username,
                                commentText = commentText,
                                date = getCurrentDateTime(),
                                profileImageRes = R.drawable.usericon
                            )

                            dbViewModel.getPostOwnerId(postId) { postOwnerId ->
                                if (postOwnerId != null) {
                                    dbViewModel.addComment(
                                        postOwnerId,
                                        postId,
                                        newComment,
                                        onComplete = { success ->
                                            if (success) {
                                                comments = comments + newComment
                                                commentText = ""
                                            }
                                        })
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C3D8D)),
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation && commentToDelete != null) {
            AlertDialog(
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White,
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Text(
                        text = "Delete comment?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This will delete the comment permanently.",
                            color = Color(0xFF757575),
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
                            onClick = { showDeleteConfirmation = false },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 16.sp, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                dbViewModel.getPostOwnerId(postId) { postOwnerId ->
                                    if (postOwnerId != null) {
                                        dbViewModel.deleteComment(
                                            postOwnerId,
                                            postId,
                                            commentToDelete!!.commentId
                                        ) { success ->
                                            if (success) {
                                                comments = comments.filterNot {
                                                    it.commentId == commentToDelete!!.commentId
                                                }
                                                showDeleteConfirmation = false
                                                commentToDelete = null
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            )
        }
    }
}



fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}



@Composable
fun CommentItem(
    comment: Comment,
    travelerId: String,
    dbViewModel: dbViewModel,
    onDeleteClicked: () -> Unit
) {
    var travelerName by remember { mutableStateOf("Loading...") }
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(comment.travelerId) {
        dbViewModel.getNameByID(comment.travelerId) { name ->
            travelerName = name ?: "Unknown Traveler"
        }

        dbViewModel.getTraveler(comment.travelerId) { traveler ->
            profileImageBase64 = traveler?.userimage
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (profileImageBase64 != null) {
            val imageBytes = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
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
            Text(text = comment.commentText, fontSize = 14.sp)
            Text(text = comment.date.substring(0, 16), fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (comment.travelerId == travelerId) {
            Icon(
                imageVector = Icons.Outlined.Backspace,
                contentDescription = "Delete Comment",
                tint = Color.Gray,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDeleteClicked() }
            )
        }
    }
}



