package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FoodCommodity
import com.example.data.model.OptimizationGoal
import com.example.data.model.PackagingMaterial
import com.example.data.model.PortfolioMetrics
import com.example.data.model.RecommendationResult
import com.example.data.model.SavedPackagingSpec
import com.example.data.repository.PackagingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PackagingUiState(
    val selectedCommodity: FoodCommodity? = null,
    val selectedGoal: OptimizationGoal = OptimizationGoal.BALANCED_ECO_PERFORMANCE,
    val targetShelfLifeDays: Int = 10,
    val recommendations: List<RecommendationResult> = emptyList(),
    val selectedRecommendation: RecommendationResult? = null,
    val aiInsightText: String = "",
    val isGeneratingAiInsight: Boolean = false,
    val isRunningRecommendation: Boolean = false,
    val isGeminiProcessing: Boolean = false,
    val geminiStatusMessage: String = "Gemini 3.5 Flash Active",
    val commoditySearchQuery: String = "",
    val commodityCategoryFilter: String = "All",
    val materialCategoryFilter: String = "All"
)

class PackagingViewModel(
    private val repository: PackagingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PackagingUiState())
    val uiState: StateFlow<PackagingUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    val allCommodities: StateFlow<List<FoodCommodity>> = repository.allCommodities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaterials: StateFlow<List<PackagingMaterial>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSpecs: StateFlow<List<SavedPackagingSpec>> = repository.allSpecs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioMetrics: StateFlow<PortfolioMetrics> = savedSpecs
        .combine(MutableStateFlow(Unit)) { specs, _ ->
            repository.calculatePortfolioMetrics(specs)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PortfolioMetrics(0, 0.0, 0.0, 0.0, 0.0)
        )

    init {
        // Auto-select initial commodity once loaded
        viewModelScope.launch {
            allCommodities.collect { list ->
                if (list.isNotEmpty() && _uiState.value.selectedCommodity == null) {
                    val initial = list.first()
                    _uiState.value = _uiState.value.copy(
                        selectedCommodity = initial,
                        targetShelfLifeDays = initial.baselineShelfLifeDays * 2
                    )
                    processPackagingParametersWithGemini(initial, _uiState.value.selectedGoal, initial.baselineShelfLifeDays * 2)
                }
            }
        }
    }

    fun selectCommodity(commodity: FoodCommodity) {
        _uiState.value = _uiState.value.copy(
            selectedCommodity = commodity,
            targetShelfLifeDays = commodity.baselineShelfLifeDays * 2,
            aiInsightText = "",
            selectedRecommendation = null
        )
        processPackagingParametersWithGemini(commodity, _uiState.value.selectedGoal, commodity.baselineShelfLifeDays * 2)
    }

    fun selectGoal(goal: OptimizationGoal) {
        _uiState.value = _uiState.value.copy(
            selectedGoal = goal,
            aiInsightText = ""
        )
        processPackagingParametersWithGemini(_uiState.value.selectedCommodity, goal, _uiState.value.targetShelfLifeDays)
    }

    fun setTargetShelfLifeDays(days: Int) {
        _uiState.value = _uiState.value.copy(targetShelfLifeDays = days)
        processPackagingParametersWithGemini(_uiState.value.selectedCommodity, _uiState.value.selectedGoal, days)
    }

    /**
     * Connects to the Gemini API to process food packaging input parameters
     * (commodity respiration, oxygen/moisture sensitivities, target OTR/WVTR, desired shelf life, optimization goal)
     * and returns recommended packaging materials with shelf-life impact scores.
     */
    fun processPackagingParametersWithGemini(
        commodity: FoodCommodity? = _uiState.value.selectedCommodity,
        goal: OptimizationGoal = _uiState.value.selectedGoal,
        targetShelfLifeDays: Int = _uiState.value.targetShelfLifeDays
    ) {
        val targetCommodity = commodity ?: return
        val materials = allMaterials.value
        if (materials.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRunningRecommendation = true,
                isGeminiProcessing = true,
                geminiStatusMessage = "Gemini processing ${targetCommodity.name} parameters..."
            )

            try {
                val results = repository.generateRecommendations(
                    commodity = targetCommodity,
                    goal = goal,
                    targetShelfLifeDays = targetShelfLifeDays,
                    materialsList = materials,
                    preferAiProcessing = true
                )

                val topRec = results.firstOrNull()
                val isAi = topRec?.isAiGenerated == true
                val status = if (isAi) "Gemini AI Recommendations Generated" else "Barrier Kinetic Model Synced"

                _uiState.value = _uiState.value.copy(
                    recommendations = results,
                    selectedRecommendation = topRec,
                    isRunningRecommendation = false,
                    isGeminiProcessing = false,
                    geminiStatusMessage = status
                )

                if (isAi) {
                    _eventFlow.emit("Gemini AI evaluated ${results.size} materials with shelf-life impact scores!")
                }
            } catch (e: Exception) {
                // Graceful fallback
                val fallbackResults = repository.generateRecommendations(
                    commodity = targetCommodity,
                    goal = goal,
                    targetShelfLifeDays = targetShelfLifeDays,
                    materialsList = materials,
                    preferAiProcessing = false
                )
                _uiState.value = _uiState.value.copy(
                    recommendations = fallbackResults,
                    selectedRecommendation = fallbackResults.firstOrNull(),
                    isRunningRecommendation = false,
                    isGeminiProcessing = false,
                    geminiStatusMessage = "Kinetic Simulation Active"
                )
            }
        }
    }

    fun runRecommendations() {
        processPackagingParametersWithGemini()
    }

    fun selectRecommendationForDetail(rec: RecommendationResult) {
        _uiState.value = _uiState.value.copy(
            selectedRecommendation = rec,
            aiInsightText = rec.aiInsight
        )
        if (rec.aiInsight.isBlank()) {
            fetchAiInsight(rec)
        }
    }

    fun fetchAiInsight(rec: RecommendationResult) {
        val commodity = _uiState.value.selectedCommodity ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingAiInsight = true)
            val insight = repository.fetchAiDeepInsight(
                commodity = commodity,
                material = rec.material,
                goal = _uiState.value.selectedGoal,
                predictedShelfLifeDays = rec.predictedShelfLifeDays,
                carbonSavingsKg = rec.carbonSavingsKgPerTon
            )
            // Update cache in recommendation item
            val updatedRecs = _uiState.value.recommendations.map {
                if (it.material.id == rec.material.id) it.copy(aiInsight = insight) else it
            }
            _uiState.value = _uiState.value.copy(
                recommendations = updatedRecs,
                selectedRecommendation = _uiState.value.selectedRecommendation?.copy(aiInsight = insight),
                aiInsightText = insight,
                isGeneratingAiInsight = false
            )
        }
    }

    fun savePackagingSpec(
        projectName: String,
        rec: RecommendationResult,
        unitCost: Double,
        moq: Int,
        supplierNotes: String,
        status: String = "APPROVED"
    ) {
        val commodity = _uiState.value.selectedCommodity ?: return
        viewModelScope.launch {
            val spec = SavedPackagingSpec(
                projectName = projectName.ifBlank { "${commodity.name} - ${rec.material.shortCode}" },
                commodityName = commodity.name,
                commodityCategory = commodity.category,
                materialName = rec.material.name,
                materialCode = rec.material.shortCode,
                baselineShelfLifeDays = commodity.baselineShelfLifeDays,
                predictedShelfLifeDays = rec.predictedShelfLifeDays,
                shelfLifeExtensionPercent = rec.shelfLifeExtensionPercent,
                ecoScore = rec.environmentalImpactScore,
                carbonSavingsKgPerTon = rec.carbonSavingsKgPerTon,
                endOfLifeMethod = rec.material.endOfLife,
                targetOtrWvtrSummary = "OTR: ${rec.material.otr} | WVTR: ${rec.material.wvtr}",
                estimatedUnitCostUsd = unitCost,
                moqUnits = moq,
                status = status,
                supplierNotes = supplierNotes,
                timestamp = System.currentTimeMillis()
            )
            repository.savePackagingSpec(spec)
            _eventFlow.emit("Specification '${spec.projectName}' saved successfully!")
        }
    }

    fun updateSpecStatus(spec: SavedPackagingSpec, newStatus: String) {
        viewModelScope.launch {
            repository.updatePackagingSpec(spec.copy(status = newStatus))
            _eventFlow.emit("Updated status to $newStatus")
        }
    }

    fun deleteSpec(spec: SavedPackagingSpec) {
        viewModelScope.launch {
            repository.deletePackagingSpec(spec)
            _eventFlow.emit("Deleted specification '${spec.projectName}'")
        }
    }

    fun addNewCommodity(
        name: String,
        category: String,
        baselineShelfLife: Int,
        idealTemp: String,
        respiration: String,
        moistureSens: Int,
        oxygenSens: Int,
        spoilageFactor: String,
        otrMin: Double,
        otrMax: Double,
        wvtrMin: Double,
        wvtrMax: Double,
        emoji: String
    ) {
        viewModelScope.launch {
            val commodity = FoodCommodity(
                name = name,
                category = category,
                baselineShelfLifeDays = baselineShelfLife,
                idealStorageTemp = idealTemp,
                respirationRate = respiration,
                moistureSensitivity = moistureSens,
                oxygenSensitivity = oxygenSens,
                lightSensitivity = 3,
                ethyleneSensitivity = 2,
                primarySpoilageFactor = spoilageFactor,
                targetOtrMin = otrMin,
                targetOtrMax = otrMax,
                targetWvtrMin = wvtrMin,
                targetWvtrMax = wvtrMax,
                iconEmoji = emoji.ifBlank { "📦" },
                description = "Custom supplier commodity profile."
            )
            val id = repository.addCommodity(commodity)
            val inserted = commodity.copy(id = id)
            selectCommodity(inserted)
            _eventFlow.emit("Added new commodity '${commodity.name}'")
        }
    }
}

class PackagingViewModelFactory(
    private val repository: PackagingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PackagingViewModel::class.java)) {
            return PackagingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
