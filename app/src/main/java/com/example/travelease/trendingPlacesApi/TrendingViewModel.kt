package com.example.travelease.trendingPlacesApi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelease.flightsApi.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import androidx.compose.runtime.mutableStateMapOf

class TrendingViewModel : ViewModel() {
    private val _destinations = MutableStateFlow<List<TrendingDestination>>(emptyList())
    val destinations: StateFlow<List<TrendingDestination>> = _destinations

    private val _noResults = MutableStateFlow(false)
    val noResults: StateFlow<Boolean> = _noResults
    private val imageCache = mutableStateMapOf<String, String>()

    fun fetchTrendingPlaces(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getTrendingDestinations(query = query)
                val destinations = response.popular_destinations?.destinations ?: emptyList()
                _destinations.value = destinations
                _noResults.value = destinations.isEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                _noResults.value = true
            }
        }
    }

    fun fetchHighQualityImage(destinationName: String, onResult: (String) -> Unit) {
        val cached = imageCache[destinationName]
        if (cached != null) {
            onResult(cached)
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getHighResImage(query = destinationName)
                val topImage = response.images_results.firstOrNull()?.original ?: ""
                if (topImage.isNotEmpty()) {
                    imageCache[destinationName] = topImage
                    onResult(topImage)
                } else {
                    onResult("")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("")
            }
        }
    }
}

