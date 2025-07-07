package com.example.travelease.firebaseDB.entities

data class Comment(
    val commentId: String = "",
    val travelerId:String="",
    val username:String="",
    val postId: String = "",
    val commentText: String = "",
    val date: String = "" ,
    val profileImageRes: Int=0
)
