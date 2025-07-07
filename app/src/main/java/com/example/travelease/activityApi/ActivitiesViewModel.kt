package com.example.travelease.activityApi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitiesViewModel : ViewModel() {

    private val repository = ActivityRepository()

    var customActivityName = mutableStateOf("")

    var startDate = mutableStateOf("")

    var endDate = mutableStateOf("")

    var searchActivityName = mutableStateOf("")

    var customActivityNameIsEmptyMessage = mutableStateOf<String?>(null)

    var searchActivityNameIsEmptyMessage =  mutableStateOf<String?>(null)

    var startDateIsEmptyMessage = mutableStateOf<String?>(null)

    var endDateIsEmptyMessage =  mutableStateOf<String?>(null)

    var endDateIsBeforeStartDateErrorMessage = mutableStateOf<String?>(null)

    var activities = mutableStateOf<List<ActivityItem>>(emptyList())
        private set


    var errorMessage = mutableStateOf("")
        private set

    fun getActivities(query: String) {
        viewModelScope.launch {
            try {
                val response = repository.fetchActivities(query)
                if (response?.events_results != null && response.events_results.isNotEmpty()) {
                    activities.value = response.events_results
                } else {
                    errorMessage.value = "No activities found"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.message}"
                e.printStackTrace()
            }
        }
    }


    fun setStartDate(date : String){
        startDate.value = date
    }

    fun setEndDate(date: String){
        endDate.value = date
    }

    fun setCustomActivityName(name: String){
        customActivityName.value = name
    }

    fun setSearchActivityName(name: String){
        searchActivityName.value = name
    }

    fun setSearchActivityNameErrorMessage(message: String?){
        searchActivityNameIsEmptyMessage.value = message
    }

    fun setCustomActivityNameErrorMessage(message: String?){
        customActivityNameIsEmptyMessage.value = message
    }

    fun setStartDateErrorMessage(message: String?){
        startDateIsEmptyMessage.value = message
    }

    fun setEndDateErrorMessage(message: String?){
        endDateIsEmptyMessage.value = message
    }

    fun setEndDateIsBeforeStartDateErrorMessage(message: String?){
        endDateIsBeforeStartDateErrorMessage.value = message
    }
    // can be used as such convertLongToTime(convertDateToLong(endDate or startDate))
    // will return the date in the format 2025-03-13 as specified in line 101
    private fun convertDateToLong(date: String): Long { // for custom activity when saving to DB
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return df.parse(date)?.time ?: 0L
    }

    private fun convertLongToTime(time: Long): String { // for custom activity when saving to DB
        val date = Date(time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }

    // TODO: add message variables and setters here
}