package com.example.spendsense.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // Ensure email is always unique
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    val email: String,
    var passwordHash: String // We'll store a "hash" not the plain password
)