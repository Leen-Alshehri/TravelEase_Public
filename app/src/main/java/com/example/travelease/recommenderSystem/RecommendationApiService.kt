package com.example.travelease.recommenderSystem

import retrofit2.http.Body
import retrofit2.http.POST

interface RecommendationApiService {
    @POST("recommend")
    suspend fun getRecommendation(
        @Body recommendationRequest: RecommendationRequest
    ): Recommendation
}