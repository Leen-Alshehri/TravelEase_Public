package com.example.travelease.firebaseDB.entities

data class Budget(
    val budgetId: String = "",
    val tripId: String = "",
    val itineraryId: String = "",
    val budget: Double = 0.0,
    val netBalance: Double = 0.0,
    val totalExpenses: Double = 0.0
)

data class Expense(
    val expenseId: String = "",
    val name: String = "",
    val amount: Float = 3f
)


