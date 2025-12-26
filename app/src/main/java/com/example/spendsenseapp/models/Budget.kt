package com.example.spendsense.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

// Add the @Entity annotation to tell Room this is a database table.
@Entity(tableName = "budgets")
data class Budget(
    // Add the @PrimaryKey annotation to define the unique ID for the table.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Changed back to Int for simplicity with autoGenerate
    val category: String,
    val amount: Double,
    val month: String // Format: "yyyy-MM"
) : Serializable {

    // The 'spent' field is calculated, not stored in the database.
    // The @Ignore annotation tells Room to skip this field.
    @Ignore
    var spent: Double = 0.0

    fun getPercentage(): Double {
        return if (amount > 0) (spent / amount) * 100 else 0.0
    }

    fun isOverBudget(): Boolean {
        return spent > amount
    }

    fun getRemaining(): Double {
        return amount - spent
    }
}