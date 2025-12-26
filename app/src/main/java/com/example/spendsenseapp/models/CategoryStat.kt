package com.example.spendsense.models

data class CategoryStat(
    val category: String,
    val amount: Double,
    val count: Int,
    val percentage: Double
)