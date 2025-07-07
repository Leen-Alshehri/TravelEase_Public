package com.example.travelease.recommenderSystem


class RecommendationRepository {
    private val api = RecommendationRetrofitClient.instance

    suspend fun fetchRecommendation(
        recommendationRequest: RecommendationRequest
    ): Recommendation? {
        return try {
            val response = api.getRecommendation(
                recommendationRequest = recommendationRequest)
        response
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("HTTP Error: ${e.code()} - $errorBody")
            null
        } catch (e: Exception) {
            println("Network Error: ${e.message}")
            null
        }
    }
}