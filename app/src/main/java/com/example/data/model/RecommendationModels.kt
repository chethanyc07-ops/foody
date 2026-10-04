package com.example.data.model

enum class OptimizationGoal(val label: String, val description: String) {
    BALANCED_ECO_PERFORMANCE(
        "Balanced Eco & Shelf Life",
        "Optimal balance between maximum freshness and minimal carbon footprint"
    ),
    MAX_SHELF_LIFE(
        "Maximum Shelf Life",
        "Prioritize superior barrier properties to extend product saleable window"
    ),
    MIN_ENVIRONMENTAL_IMPACT(
        "Net-Zero & Compostable",
        "Prioritize lowest carbon emissions, home biodegradability, and non-fossil resins"
    ),
    COST_CONSCIOUS_GREEN(
        "Cost-Effective Sustainable",
        "Best sustainable packaging within competitive commercial unit cost limits"
    )
}

data class RecommendationResult(
    val material: PackagingMaterial,
    val predictedShelfLifeDays: Int,
    val shelfLifeExtensionPercent: Double,
    val shelfLifeImpactScore: Int = 80, // 0 - 100 shelf-life preservation score
    val environmentalImpactScore: Int, // 0 - 100 (higher is cleaner/greener)
    val carbonSavingsKgPerTon: Double, // kg CO2-eq saved vs virgin fossil plastic baseline
    val degradationRating: String, // Fast (Home), Moderate (Industrial), Recyclable
    val barrierMatchScore: Int, // 0 - 100
    val economicScore: Int, // 0 - 100
    val overallScore: Int, // 0 - 100
    val rankBadge: String, // "Top Recommendation", "Eco-Champion", "High-Barrier Pick", "Cost Optimizer"
    val barrierAnalysisNotes: String,
    val aiInsight: String = "",
    val suggestedActiveAdditives: String = "",
    val isAiGenerated: Boolean = false
)

data class PortfolioMetrics(
    val totalApprovedSpecs: Int,
    val averageEcoScore: Double,
    val averageShelfLifeExtensionDays: Double,
    val totalCarbonReductionTons: Double,
    val circularMaterialPercentage: Double
)
