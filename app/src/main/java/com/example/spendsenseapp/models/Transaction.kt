package com.example.spendsense.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Room will generate this, so it has a default
    val title: String,
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val date: String, // Stored as "yyyy-MM-dd"
    val note: String = ""
) : Serializable // Implementing Serializable to easily pass it via intents