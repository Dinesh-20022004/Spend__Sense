package com.example.spendsense.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val category: String,
    val amount: Double,
    val month: String,
    val userEmail: String // <-- NEW COLUMN
) : Serializable {

    @Ignore
    var spent: Double = 0.0

    fun getPercentage(): Double = if (amount > 0) (spent / amount) * 100 else 0.0
    fun isOverBudget(): Boolean = spent > amount
    fun getRemaining(): Double = amount - spent
}