package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_packaging_specs")
data class SavedPackagingSpec(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectName: String,
    val commodityName: String,
    val commodityCategory: String,
    val materialName: String,
    val materialCode: String,
    val baselineShelfLifeDays: Int,
    val predictedShelfLifeDays: Int,
    val shelfLifeExtensionPercent: Double,
    val ecoScore: Int,
    val carbonSavingsKgPerTon: Double,
    val endOfLifeMethod: String,
    val targetOtrWvtrSummary: String,
    val estimatedUnitCostUsd: Double,
    val moqUnits: Int = 10000,
    val status: String = "APPROVED", // DRAFT, APPROVED, IN_PRODUCTION
    val supplierNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
