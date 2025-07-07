package com.example.travelease.firebaseDB.entities

data class Post(
    val postId: String = "",
    val travelerId: String = "",
    val publicationDate: String = "",
    val postText: String = "",
    val imageUrl: String? = null,
    val imageRes: String? =null,
    val profileImageRes: Int?=null,
    val totalLikes: Int = 0,
    var totalComments: Int = 0,
    val locationName: String = "",  // Store location as a name
    val city:String="",
    var likedBy: List<String> = emptyList() //list of travelers id
)
