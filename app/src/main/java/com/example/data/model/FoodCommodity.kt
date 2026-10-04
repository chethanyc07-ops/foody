package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_commodities")
data class FoodCommodity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Fresh Produce, Meat & Poultry, Seafood, Bakery, Dairy, Dry Staples
    val baselineShelfLifeDays: Int,
    val idealStorageTemp: String,
    val respirationRate: String, // Low, Medium, High, Very High, Extreme
    val moistureSensitivity: Int, // 1 to 5
    val oxygenSensitivity: Int,   // 1 to 5
    val lightSensitivity: Int,    // 1 to 5
    val ethyleneSensitivity: Int, // 1 to 5
    val primarySpoilageFactor: String,
    val targetOtrMin: Double, // cc/m²/day
    val targetOtrMax: Double,
    val targetWvtrMin: Double, // g/m²/day
    val targetWvtrMax: Double,
    val iconEmoji: String = "🍎",
    val description: String = ""
)
