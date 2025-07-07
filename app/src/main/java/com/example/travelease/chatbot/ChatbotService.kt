package com.example.travelease.chatbot

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class Message( // represent messages in chat
    val role: String, //user or system
    val content: String // the message
)

data class ChatRequest( //API request
    val model: String = "llama3-70b-8192", //LLaMA 3 model
    val messages: List<Message>, //full conversation context , user and system
    val temperature: Float = 0.7f //controls response creativity (0 = more predictable, 1 = more creative), 0.7 gives balanced responses
)

data class Choice(
    val message: Message
)

data class ChatResponse( //API response
    val choices: List<Choice> //full structure of the response from Groq contains a list of possible chatbot responses
)

interface ChatbotService {
    @Headers(
        "REMOVED", // API key
        "Content-Type: application/json" //tells server we're sending JSON
    )
    @POST("v1/chat/completions")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse> //send request and return response
}

object ChatbotApi {
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) //this timeout gives the model enough time to generate a meaningful response
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/") // Groq API
        .addConverterFactory(GsonConverterFactory.create()) //convert JSON responses to Kotlin
        .client(okHttpClient)
        .build()

    val chatbotService: ChatbotService = retrofit.create(ChatbotService::class.java)
}
