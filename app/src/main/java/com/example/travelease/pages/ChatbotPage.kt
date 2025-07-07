package com.example.travelease.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.util.Log
import com.airbnb.lottie.compose.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.travelease.chatbot.ChatRequest
import com.example.travelease.chatbot.Message
import com.example.travelease.chatbot.ChatbotApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun ChatbotPage(modifier: Modifier = Modifier,navController: NavController) {

    // state variables for message input and conversation history
    var messageText by remember { mutableStateOf(TextFieldValue("")) } //Track current input typed by the user
    var chatMessages by remember { mutableStateOf(listOf<ChatMessage>()) } //Hold conversation messages
    val coroutineScope = rememberCoroutineScope() //to launch coroutines inside composable
    val listState = rememberLazyListState() //Tracks scroll position of chat messages screen
    var isTyping by remember { mutableStateOf(false) } //Indicates whether the bot is generating a response or not
    var showWelcomeText by remember { mutableStateOf(true) }// indicate whether to show welcoming message or not
    val lifecycleOwner = LocalLifecycleOwner.current // to observe page state

    // Lottie chatbot icon setup
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("chatbot_icon.json")) // loads it from assets
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever) //animate it

    //clear chat messages when user leaves the page/session
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                chatMessages = emptyList()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // layout container & page style
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(Color.White)
    ) {

        Text(
            text = "TravelEase AI",
            fontSize = 35.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(16.dp),
            style = TextStyle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF349EFF), Color(0xFF0C3D8D))
                )
            )
        )

        Divider(color = Color.Black, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

        // Welcoming animation text
        AnimatedVisibility(
            visible = showWelcomeText,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF349EFF), Color(0xFF0C3D8D))
                                )
                            )
                        ) {
                            append("Ask whatever ")
                        }
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("\nyou want help with...")
                        }
                    },
                    fontSize = 33.sp,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }



        //Chat messages list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 100.dp),
            state = listState
        ) {
            //this show messages in chat bubble
            items(chatMessages) { message ->
                ChatBubble(message)
            }
            if (isTyping) {
                item {
                    ChatBubble(ChatMessage("TravelEase AI is typing...", isBot = true))
                }
            }
        }


        //Message input row "user's input"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .offset(y = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(25.dp),
                placeholder = { Text("Type your message...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { //this for send from keyboard
                        if (messageText.text.isNotBlank()) {
                            val userMessage = messageText.text

                            //add user's message to the chat list
                            coroutineScope.launch {
                                chatMessages = chatMessages + ChatMessage(userMessage, isBot = false) //is bot indicate who wrote the message
                                isTyping = true //means bot is generating response and shows "is Typing....."

                                val botResponse = withContext(Dispatchers.IO) {// create a thread to generate response
                                    getChatbotResponse(userMessage) //get bot response
                                }
                                isTyping = false //done generating response

                                chatMessages = chatMessages + ChatMessage(botResponse, isBot = true) //add the bot message to the list
                                listState.animateScrollToItem(chatMessages.size - 1) //scroll down
                            }
                            messageText = TextFieldValue("") //empty the input text field to allow user send again
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            //same as previous but this for the actual send button
            Button(
                onClick = {
                    val userMessage = messageText.text.trim()
                    if (userMessage.isNotBlank()) {
                        showWelcomeText = false
                        coroutineScope.launch {
                            chatMessages = chatMessages + ChatMessage(userMessage, isBot = false)

                            isTyping = true

                            val botResponse = withContext(Dispatchers.IO) {
                                getChatbotResponse(userMessage)
                            }

                            isTyping = false

                            chatMessages = chatMessages + ChatMessage(botResponse, isBot = true)
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                        messageText = TextFieldValue("")
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
        // Lottie chatbot icon animated
        LottieAnimation(
            composition = composition,
            progress = progress,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.BottomStart)
                .offset(x = 2.dp, y = (-90).dp)
        )
    }

}//end of ChatbotPage

// Data class for chat messages
data class ChatMessage(val text: String, val isBot: Boolean)

// The Chat bubble component
@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (message.isBot) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            shape = if (message.isBot) {
                // Chatbot
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomEnd = 20.dp,
                    bottomStart = 0.dp
                )
            } else {
                // User
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 0.dp
                )
            },
            color = if (message.isBot) Color.White else Color(0xFFF3F3F5),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                letterSpacing = 0.2.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}


//Function to Call CROQ API
suspend fun getChatbotResponse(userMessage: String): String {
    return try {
        val messages = listOf( // conversation history as a list of Messages
            Message(role = "system", content = "You are a helpful travel assistant. Only answer questions related to travel. If the question is not about travel, politely decline to answer."), //Sets behavior of the chatbot . It tells the model how to act
            Message(role = "user", content = userMessage) //user message to respond to
        )

        val request = ChatRequest(messages = messages)

        val response = ChatbotApi.chatbotService.sendMessage(request).execute() //Sends a synchronous HTTP request using Retrofit to the API.

        if (response.isSuccessful) { // returned status code 200 OK
            val reply = response.body()?.choices?.firstOrNull()?.message?.content //Extracts the actual reply from the API, choices is list of possible completions (usually 1), message.content is the generated response
            reply?.trim() ?: "No response from API"
        } else {
            val errorBody = response.errorBody()?.string() //extract the error message
            Log.e("APIGroqError", "HTTP ${response.code()} - $errorBody")
            "Error ${response.code()}: $errorBody"
        }

    } catch (e: Exception) {
        Log.e("GroqException", "API call failed", e)
        "Error: ${e.message ?: e.javaClass.name}"
    }
}
