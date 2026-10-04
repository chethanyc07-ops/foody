package com.example.data.repository

import com.example.data.ai.GeminiPackagingService
import com.example.data.local.CommodityDao
import com.example.data.local.MaterialDao
import com.example.data.local.PackagingSpecDao
import com.example.data.model.FoodCommodity
import com.example.data.model.OptimizationGoal
import com.example.data.model.PackagingMaterial
import com.example.data.model.PortfolioMetrics
import com.example.data.model.RecommendationResult
import com.example.data.model.SavedPackagingSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PackagingRepository(
    private val commodityDao: CommodityDao,
    private val materialDao: MaterialDao,
    private val specDao: PackagingSpecDao,
    private val geminiService: GeminiPackagingService = GeminiPackagingService()
) {

    val allCommodities: Flow<List<FoodCommodity>> = commodityDao.getAllCommodities()
    val allMaterials: Flow<List<PackagingMaterial>> = materialDao.getAllMaterials()
    val allSpecs: Flow<List<SavedPackagingSpec>> = specDao.getAllSpecs()

    suspend fun addCommodity(commodity: FoodCommodity): Long {
        return commodityDao.insertCommodity(commodity)
    }

    suspend fun addMaterial(material: PackagingMaterial): Long {
        return materialDao.insertMaterial(material)
    }

    suspend fun savePackagingSpec(spec: SavedPackagingSpec): Long {
        return specDao.insertSpec(spec)
    }

    suspend fun updatePackagingSpec(spec: SavedPackagingSpec) {
        specDao.updateSpec(spec)
    }

    suspend fun deletePackagingSpec(spec: SavedPackagingSpec) {
        specDao.deleteSpec(spec)
    }

    fun calculatePortfolioMetrics(specs: List<SavedPackagingSpec>): PortfolioMetrics {
        if (specs.isEmpty()) {
            return PortfolioMetrics(
                totalApprovedSpecs = 0,
                averageEcoScore = 0.0,
                averageShelfLifeExtensionDays = 0.0,
                totalCarbonReductionTons = 0.0,
                circularMaterialPercentage = 0.0
            )
        }

        val approvedSpecs = specs.filter { it.status == "APPROVED" || it.status == "IN_PRODUCTION" }
        val count = approvedSpecs.size.coerceAtLeast(1)
        val avgEco = approvedSpecs.map { it.ecoScore }.average()
        val avgExtDays = approvedSpecs.map { (it.predictedShelfLifeDays - it.baselineShelfLifeDays).toDouble() }.average()
        val totalCarbonTons = approvedSpecs.sumOf { it.carbonSavingsKgPerTon * (it.moqUnits / 1000.0) / 1000.0 }
        val circularCount = approvedSpecs.count {
            it.endOfLifeMethod.contains("Compost", ignoreCase = true) ||
                    it.endOfLifeMethod.contains("Recyclable", ignoreCase = true)
        }
        val circularPct = (circularCount.toDouble() / count.toDouble()) * 100.0

        return PortfolioMetrics(
            totalApprovedSpecs = approvedSpecs.size,
            averageEcoScore = avgEco,
            averageShelfLifeExtensionDays = avgExtDays,
            totalCarbonReductionTons = totalCarbonTons,
            circularMaterialPercentage = circularPct
        )
    }

    suspend fun generateRecommendations(
        commodity: FoodCommodity,
        goal: OptimizationGoal,
        targetShelfLifeDays: Int? = null,
        materialsList: List<PackagingMaterial>? = null,
        preferAiProcessing: Boolean = true
    ): List<RecommendationResult> {
        val materials = materialsList ?: materialDao.getAllMaterials().first()
        if (materials.isEmpty()) return emptyList()

        val targetDays = targetShelfLifeDays ?: (commodity.baselineShelfLifeDays * 2)

        if (preferAiProcessing) {
            val aiResults = geminiService.processFoodPackagingWithGemini(
                commodity = commodity,
                goal = goal,
                targetShelfLifeDays = targetDays,
                candidateMaterials = materials
            )
            if (!aiResults.isNullOrEmpty()) {
                return aiResults
            }
        }

        val results = materials.map { material ->
            computeSingleRecommendation(commodity, material, goal, targetDays)
        }

        // Sort by overall score descending
        val sorted = results.sortedByDescending { it.overallScore }

        // Find key champions for badges
        val topOverall = sorted.firstOrNull()
        val topEco = sorted.maxByOrNull { it.environmentalImpactScore }
        val topFresh = sorted.maxByOrNull { it.predictedShelfLifeDays }
        val topEcon = sorted.maxByOrNull { it.economicScore }

        return sorted.mapIndexed { index, item ->
            val badge = when {
                item == topOverall -> "⭐ Top Recommendation"
                item == topEco && item != topOverall -> "🌿 Eco-Champion"
                item == topFresh && item != topOverall -> "⏱️ Freshness Master"
                item == topEcon && item != topOverall -> "💰 Commercial Value"
                index < 3 -> "High Match (#${index + 1})"
                else -> "Alternative Option"
            }
            item.copy(rankBadge = badge)
        }
    }

    private fun computeSingleRecommendation(
        commodity: FoodCommodity,
        material: PackagingMaterial,
        goal: OptimizationGoal,
        targetShelfLifeDays: Int?
    ): RecommendationResult {
        // 1. Barrier Match Score (0 - 100)
        val otrMatch = calculateBarrierDimensionScore(
            actual = material.otr,
            minTarget = commodity.targetOtrMin,
            maxTarget = commodity.targetOtrMax
        )
        val wvtrMatch = calculateBarrierDimensionScore(
            actual = material.wvtr,
            minTarget = commodity.targetWvtrMin,
            maxTarget = commodity.targetWvtrMax
        )

        // Weight by commodity sensitivity
        val o2Weight = commodity.oxygenSensitivity.toDouble() / 5.0
        val h2oWeight = commodity.moistureSensitivity.toDouble() / 5.0
        val totalSensWeight = (o2Weight + h2oWeight).coerceAtLeast(0.1)
        val barrierScore = (((otrMatch * o2Weight) + (wvtrMatch * h2oWeight)) / totalSensWeight).roundToInt().coerceIn(15, 100)

        // 2. Shelf Life Extension Kinetics
        // Baseline shelf life multiplied by barrier efficiency factor
        val efficiencyFactor = (barrierScore / 100.0)
        val maxPotentialMultiplier = when (commodity.category) {
            "Fresh Produce" -> 2.2 // e.g. 4 -> 9 days
            "Meat & Poultry" -> 2.8 // e.g. 5 -> 14 days
            "Seafood" -> 2.5 // e.g. 4 -> 10 days
            "Bakery" -> 2.0 // e.g. 3 -> 6 days
            "Dairy" -> 2.0 // e.g. 30 -> 60 days
            else -> 1.8
        }

        val multiplier = 1.0 + ((maxPotentialMultiplier - 1.0) * efficiencyFactor)
        val predictedDays = (commodity.baselineShelfLifeDays * multiplier).roundToInt()
        val extensionPercent = ((predictedDays - commodity.baselineShelfLifeDays).toDouble() / commodity.baselineShelfLifeDays.toDouble()) * 100.0

        // 3. Environmental Impact Score (0 - 100, higher = greener)
        val carbonScore = ((1.0 - (material.carbonFootprintKgCo2 / 4.8).coerceIn(0.0, 1.0)) * 100.0)
        val endOfLifeScore = when {
            material.endOfLife.contains("Home", ignoreCase = true) || material.endOfLife.contains("Edible", ignoreCase = true) -> 100.0
            material.endOfLife.contains("Industrial", ignoreCase = true) -> 82.0
            material.endOfLife.contains("Recyclable", ignoreCase = true) -> 80.0
            else -> 50.0
        }
        val circularityComponent = material.circularityScore.toDouble()
        val ecoScore = (0.40 * carbonScore + 0.35 * endOfLifeScore + 0.25 * circularityComponent).roundToInt().coerceIn(10, 100)

        // Carbon savings vs standard virgin polymer baseline (4.5 kg CO2e / kg)
        val carbonSavingsKgPerTon = max(0.0, (4.5 - material.carbonFootprintKgCo2) * 1000.0)

        // 4. Economic Score (0 - 100, lower cost = higher score)
        // Normalizing $1.20/kg -> 100, $5.00/kg -> 30
        val costScore = ((1.0 - ((material.costPerKgUsd - 1.2).coerceAtLeast(0.0) / 4.0).coerceIn(0.0, 0.8)) * 100.0).roundToInt().coerceIn(20, 100)

        // 5. Shelf life score normalized against user expectation or baseline
        val targetDays = targetShelfLifeDays ?: (commodity.baselineShelfLifeDays * 2)
        val shelfLifeScore = ((predictedDays.toDouble() / targetDays.toDouble()).coerceIn(0.2, 1.5) * 70.0).roundToInt().coerceIn(15, 100)

        // 6. Overall Multi-Objective Score according to OptimizationGoal
        val overall = when (goal) {
            OptimizationGoal.BALANCED_ECO_PERFORMANCE -> {
                (0.35 * barrierScore + 0.35 * ecoScore + 0.15 * shelfLifeScore + 0.15 * costScore).roundToInt()
            }
            OptimizationGoal.MAX_SHELF_LIFE -> {
                (0.45 * shelfLifeScore + 0.35 * barrierScore + 0.15 * ecoScore + 0.05 * costScore).roundToInt()
            }
            OptimizationGoal.MIN_ENVIRONMENTAL_IMPACT -> {
                (0.55 * ecoScore + 0.20 * barrierScore + 0.15 * shelfLifeScore + 0.10 * costScore).roundToInt()
            }
            OptimizationGoal.COST_CONSCIOUS_GREEN -> {
                (0.40 * costScore + 0.30 * ecoScore + 0.20 * barrierScore + 0.10 * shelfLifeScore).roundToInt()
            }
        }.coerceIn(10, 100)

        val degradationRating = when {
            material.degradationDays <= 60 -> "Rapid Compost (~${material.degradationDays}d)"
            material.degradationDays <= 180 -> "Medium Bio-degrade (~${material.degradationDays}d)"
            else -> "Closed-Loop Circular"
        }

        val barrierNotes = "OTR: ${material.otr} cc/m² (Target: ${commodity.targetOtrMin.roundToInt()}-${commodity.targetOtrMax.roundToInt()}), " +
                "WVTR: ${material.wvtr} g/m² (Target: ${commodity.targetWvtrMin.roundToInt()}-${commodity.targetWvtrMax.roundToInt()})"

        val suggestedAdditives = when {
            commodity.category == "Fresh Produce" && commodity.respirationRate.contains("High", ignoreCase = true) ->
                "Integrate 1% potassium permanganate ethylene scavenger sachets or micro-perforations."
            commodity.category == "Meat & Poultry" || commodity.category == "Seafood" ->
                "Apply rosemary extract antimicrobial coating & food-grade drip-absorbent core layer."
            commodity.category == "Bakery" ->
                "Utilize ethanol-emitter vapor strip or bio-based moisture regulation silica pad."
            else ->
                "Standard inert nitrogen gas flush (<0.5% residual O2)."
        }

        val shelfLifeImpactScore = ((predictedDays.toDouble() / (commodity.baselineShelfLifeDays * 2.5)).coerceIn(0.2, 1.0) * 100.0).roundToInt().coerceIn(30, 99)

        return RecommendationResult(
            material = material,
            predictedShelfLifeDays = predictedDays,
            shelfLifeExtensionPercent = extensionPercent,
            shelfLifeImpactScore = shelfLifeImpactScore,
            environmentalImpactScore = ecoScore,
            carbonSavingsKgPerTon = carbonSavingsKgPerTon,
            degradationRating = degradationRating,
            barrierMatchScore = barrierScore,
            economicScore = costScore,
            overallScore = overall,
            rankBadge = "",
            barrierAnalysisNotes = barrierNotes,
            aiInsight = "",
            suggestedActiveAdditives = suggestedAdditives,
            isAiGenerated = false
        )
    }

    private fun calculateBarrierDimensionScore(
        actual: Double,
        minTarget: Double,
        maxTarget: Double
    ): Double {
        if (actual in minTarget..maxTarget) {
            return 100.0
        }
        val distanceRatio = if (actual < minTarget) {
            (minTarget - actual) / minTarget.coerceAtLeast(1.0)
        } else {
            (actual - maxTarget) / maxTarget.coerceAtLeast(1.0)
        }
        // Logarithmic decay penalty
        val penalty = (ln(1.0 + distanceRatio) * 35.0).coerceIn(0.0, 80.0)
        return (100.0 - penalty).coerceIn(20.0, 100.0)
    }

    suspend fun fetchAiDeepInsight(
        commodity: FoodCommodity,
        material: PackagingMaterial,
        goal: OptimizationGoal,
        predictedShelfLifeDays: Int,
        carbonSavingsKg: Double
    ): String {
        return geminiService.generateAiPackagingAnalysis(
            commodity = commodity,
            material = material,
            goal = goal,
            predictedShelfLifeDays = predictedShelfLifeDays,
            carbonSavingsKg = carbonSavingsKg
        )
    }
}
