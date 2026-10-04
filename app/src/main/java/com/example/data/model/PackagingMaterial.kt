package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packaging_materials")
data class PackagingMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortCode: String,
    val category: String, // Bio-based Compostable, Recycled Polymer, Fiber/Cellulose, High-Barrier Mono-Material, Active Bio-Coating
    val description: String,
    // Barrier properties
    val otr: Double, // cc/m²/day/atm at 23°C, 0% RH
    val wvtr: Double, // g/m²/day at 38°C, 90% RH
    val co2Permeability: Double, // cc/m²/day
    val tensileStrengthMpa: Double, // Mechanical strength
    // Environmental properties
    val carbonFootprintKgCo2: Double, // kg CO2-eq per kg of resin/material
    val endOfLife: String, // Home Compostable, Industrial Compostable, Curbside Recyclable, Marine Biodegradable
    val degradationDays: Int, // approx days to degrade in designated environment
    val circularityScore: Int, // 0 - 100 based on renewability and recycled content
    // Economic & Technical properties
    val costPerKgUsd: Double,
    val minTempCelsius: Int,
    val maxTempCelsius: Int,
    val certifications: String, // FDA 21 CFR, BPI, EN 13432, OK Compost
    val bestSuitedFor: String
)
