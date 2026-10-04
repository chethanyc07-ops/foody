package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FoodCommodity
import com.example.data.model.OptimizationGoal
import com.example.data.model.PackagingMaterial
import com.example.data.model.RecommendationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiPackagingService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun processFoodPackagingWithGemini(
        commodity: FoodCommodity,
        goal: OptimizationGoal,
        targetShelfLifeDays: Int,
        candidateMaterials: List<PackagingMaterial>
    ): List<RecommendationResult>? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiService", "No custom Gemini API key provided, falling back to algorithmic solver.")
            return@withContext null
        }

        val materialsCatalogText = candidateMaterials.joinToString("\n") { m ->
            "- shortCode: \"${m.shortCode}\", name: \"${m.name}\", category: \"${m.category}\", OTR: ${m.otr}, WVTR: ${m.wvtr}, carbon: ${m.carbonFootprintKgCo2} kg/kg, endOfLife: \"${m.endOfLife}\", degradationDays: ${m.degradationDays}, circularity: ${m.circularityScore}, costPerKg: ${m.costPerKgUsd}"
        }

        val prompt = """
            You are a leading Food Packaging Scientist and Kinetic Simulation AI.
            Process the following food commodity packaging input parameters:
            - Commodity Name: ${commodity.name}
            - Category: ${commodity.category}
            - Baseline Shelf Life: ${commodity.baselineShelfLifeDays} days
            - Ideal Storage Condition: ${commodity.idealStorageTemp}
            - Respiration Rate: ${commodity.respirationRate}
            - Moisture Sensitivity (1-5): ${commodity.moistureSensitivity}
            - Oxygen Sensitivity (1-5): ${commodity.oxygenSensitivity}
            - Primary Spoilage Factor: ${commodity.primarySpoilageFactor}
            - Target Permeability: OTR [${commodity.targetOtrMin} - ${commodity.targetOtrMax} cc/m²/d], WVTR [${commodity.targetWvtrMin} - ${commodity.targetWvtrMax} g/m²/d]
            - Optimization Goal: ${goal.label} (${goal.description})
            - Target Desired Shelf Life: $targetShelfLifeDays days

            Available Packaging Materials:
            $materialsCatalogText

            Task:
            Evaluate each candidate packaging material against this commodity.
            Compute for each material:
            1. predictedShelfLifeDays (realistic shelf life in days under ${commodity.idealStorageTemp})
            2. shelfLifeImpactScore (integer 0-100 indicating preservation effectiveness)
            3. environmentalImpactScore (integer 0-100 based on carbon emissions & end-of-life)
            4. barrierMatchScore (integer 0-100 matching actual OTR/WVTR against commodity targets)
            5. overallScore (integer 0-100 weighted according to ${goal.label})
            6. rankBadge (e.g. "⭐ Top AI Pick", "🌿 Eco-Champion", "⏱️ Shelf-Life Master", "💰 Commercial Value")
            7. barrierAnalysisNotes (concise note on OTR/WVTR kinetic suitability)
            8. aiInsight (1-2 sentences of scientific rationale)
            9. suggestedActiveAdditives (e.g. ethylene scavenger, antimicrobial pad, moisture sachet)

            Respond ONLY with a valid JSON array of objects sorted by overallScore descending:
            [
              {
                "shortCode": "string",
                "predictedShelfLifeDays": 8,
                "shelfLifeImpactScore": 88,
                "environmentalImpactScore": 92,
                "barrierMatchScore": 85,
                "overallScore": 91,
                "rankBadge": "⭐ Top AI Pick",
                "barrierAnalysisNotes": "string",
                "aiInsight": "string",
                "suggestedActiveAdditives": "string"
              }
            ]
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                    put("topK", 32)
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val candidates = root.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val rawJson = parts?.optJSONObject(0)?.optString("text")

                    if (!rawJson.isNullOrBlank()) {
                        val parsedArray = JSONArray(rawJson)
                        val materialsByCode = candidateMaterials.associateBy { it.shortCode }
                        val results = mutableListOf<RecommendationResult>()

                        for (i in 0 until parsedArray.length()) {
                            val item = parsedArray.getJSONObject(i)
                            val code = item.optString("shortCode")
                            val material = materialsByCode[code] ?: candidateMaterials.firstOrNull { it.name.contains(code, ignoreCase = true) }

                            if (material != null) {
                                val predictedDays = item.optInt("predictedShelfLifeDays", commodity.baselineShelfLifeDays * 2)
                                val shelfLifeImpact = item.optInt("shelfLifeImpactScore", 85)
                                val ecoScore = item.optInt("environmentalImpactScore", material.circularityScore)
                                val barrierScore = item.optInt("barrierMatchScore", 80)
                                val overall = item.optInt("overallScore", 85)
                                val badge = item.optString("rankBadge", if (i == 0) "⭐ Top AI Pick" else "Alternative Option")
                                val barrierNotes = item.optString("barrierAnalysisNotes", "OTR: ${material.otr} | WVTR: ${material.wvtr}")
                                val insight = item.optString("aiInsight", "")
                                val additives = item.optString("suggestedActiveAdditives", "")

                                val extensionPct = ((predictedDays - commodity.baselineShelfLifeDays).toDouble() / commodity.baselineShelfLifeDays.toDouble()) * 100.0
                                val carbonSavings = kotlin.math.max(0.0, (4.5 - material.carbonFootprintKgCo2) * 1000.0)
                                val costScore = ((1.0 - ((material.costPerKgUsd - 1.2).coerceAtLeast(0.0) / 4.0).coerceIn(0.0, 0.8)) * 100.0).toInt().coerceIn(20, 100)

                                val degradationRating = when {
                                    material.degradationDays <= 60 -> "Rapid Compost (~${material.degradationDays}d)"
                                    material.degradationDays <= 180 -> "Medium Bio-degrade (~${material.degradationDays}d)"
                                    else -> "Closed-Loop Circular"
                                }

                                results.add(
                                    RecommendationResult(
                                        material = material,
                                        predictedShelfLifeDays = predictedDays,
                                        shelfLifeExtensionPercent = extensionPct,
                                        shelfLifeImpactScore = shelfLifeImpact,
                                        environmentalImpactScore = ecoScore,
                                        carbonSavingsKgPerTon = carbonSavings,
                                        degradationRating = degradationRating,
                                        barrierMatchScore = barrierScore,
                                        economicScore = costScore,
                                        overallScore = overall,
                                        rankBadge = badge,
                                        barrierAnalysisNotes = barrierNotes,
                                        aiInsight = insight,
                                        suggestedActiveAdditives = additives,
                                        isAiGenerated = true
                                    )
                                )
                            }
                        }

                        if (results.isNotEmpty()) {
                            return@withContext results
                        }
                    }
                }
            } else {
                Log.w("GeminiService", "Gemini recommend failed with code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error during Gemini recommendation call", e)
        }

        null
    }

    suspend fun generateAiPackagingAnalysis(
        commodity: FoodCommodity,
        material: PackagingMaterial,
        goal: OptimizationGoal,
        predictedShelfLifeDays: Int,
        carbonSavingsKg: Double
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineAnalyticalRationale(commodity, material, goal, predictedShelfLifeDays)
        }

        val prompt = """
            You are a senior Food Packaging Materials Scientist and Life Cycle Assessment (LCA) engineer.
            Provide an authoritative, technical supplier packaging assessment for:
            
            Food Commodity: ${commodity.name} (${commodity.category})
            - Baseline shelf life: ${commodity.baselineShelfLifeDays} days
            - Spoilage factor: ${commodity.primarySpoilageFactor}
            - Respiration rate: ${commodity.respirationRate}
            - Target OTR: ${commodity.targetOtrMin} - ${commodity.targetOtrMax} cc/m²/day
            - Target WVTR: ${commodity.targetWvtrMin} - ${commodity.targetWvtrMax} g/m²/day
            
            Selected Packaging Material: ${material.name} (${material.shortCode})
            - Category: ${material.category}
            - Material OTR: ${material.otr} cc/m²/day | WVTR: ${material.wvtr} g/m²/day
            - Carbon Footprint: ${material.carbonFootprintKgCo2} kg CO2/kg
            - End-of-Life: ${material.endOfLife} (Degradation: ~${material.degradationDays} days)
            - Circularity Score: ${material.circularityScore}/100
            
            Simulated Shelf Life: $predictedShelfLifeDays days (${goal.label})
            Carbon Saved vs Virgin Plastic: $carbonSavingsKg kg CO2e / metric ton.
            
            In 3 concise bullet points formatted with markdown:
            1. Barrier Kinetics Match (explain how OTR & WVTR interact with this commodity's respiration or decay)
            2. Life Cycle & Circularity Advantage (highlight end-of-life benefits and carbon mitigation)
            3. Actionable Supplier Recommendation (active packaging additives, sealing specs, or anti-fog coatings to consider)
            
            Keep the response punchy, scientifically rigorous, and under 160 words.
        """.trimIndent()

        try {
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.4)
                    put("topK", 32)
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) {
                                return@withContext text.trim()
                            }
                        }
                    }
                }
            } else {
                Log.w("GeminiService", "API call failed with code: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Exception during Gemini request", e)
        }

        // Graceful fallback to offline scientific rationale
        getOfflineAnalyticalRationale(commodity, material, goal, predictedShelfLifeDays)
    }

    private fun getOfflineAnalyticalRationale(
        commodity: FoodCommodity,
        material: PackagingMaterial,
        goal: OptimizationGoal,
        predictedShelfLifeDays: Int
    ): String {
        return """
            • **Barrier Kinetics Match:** ${material.name} provides OTR of ${material.otr} cc/m²/day and WVTR of ${material.wvtr} g/m²/day, effectively controlling ${commodity.primarySpoilageFactor} to reach a stable ${predictedShelfLifeDays}-day shelf life.
            • **Circularity & Carbon:** Emits only ${material.carbonFootprintKgCo2} kg CO2e/kg with a circularity index of ${material.circularityScore}/100. Degrades cleanly via ${material.endOfLife} within ~${material.degradationDays} days.
            • **Supplier Action Item:** Optimize seal temperature between ${material.minTempCelsius}°C and ${material.maxTempCelsius}°C. Consider micro-perforation or natural bio-coating for balanced equilibrium modified atmosphere.
        """.trimIndent()
    }
}
