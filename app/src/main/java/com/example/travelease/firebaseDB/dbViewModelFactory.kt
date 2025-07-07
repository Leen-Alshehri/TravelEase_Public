package com.example.travelease.firebaseDB

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class dbViewModelFactory(private val repository: dbRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(dbViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return dbViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
