package com.example.travelease.accommodationsApi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AccommodationViewModel : ViewModel() {
    private val repository = AccommodationRepository()

    var destination = mutableStateOf("Destination")

    var checkInDate = mutableStateOf("")

    var checkOutDate = mutableStateOf("")

    var expanded = mutableStateOf(false)

    var accommodations = mutableStateOf<List<Property>>(emptyList())
        private set
    var errorMessage = mutableStateOf("")
        private set
    var sortBy = mutableStateOf(13) // default to most reviewed

    // New loading state
    var isLoading = mutableStateOf(false)
        private set


    fun getAccommodations() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = repository.fetchAccommodations(
                    query = destination.value,
                    checkInDate = convertLongToTime(convertDateToLong(checkInDate.value)),
                    checkOutDate = convertLongToTime(convertDateToLong(checkOutDate.value)),
                    sortBy = sortBy.value
                )

                if (response?.properties != null) {
                    if (response.properties.isNotEmpty()) {
                        accommodations.value = response.properties
                        errorMessage.value = ""
                    } else {
                        accommodations.value = emptyList()
                        errorMessage.value = "No accommodations found"
                    }
                } else {
                    accommodations.value = emptyList()
                    errorMessage.value = "No accommodations found"
                }
            } catch (e: Exception) {
                accommodations.value = emptyList()
                errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }


    fun updateSortBy(newSort: Int) {
        if (sortBy.value != newSort) {
            sortBy.value = newSort
            getAccommodations()
        }
    }

    fun setCheckInDate(date : String){
        checkInDate.value = date
    }

    fun setCheckOutDate(date: String){
        checkOutDate.value = date
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }
}


