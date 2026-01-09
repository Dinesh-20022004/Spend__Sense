package com.example.spendsense.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String = "",
    val userEmail: String // <-- NEW COLUMN
) : Serializable