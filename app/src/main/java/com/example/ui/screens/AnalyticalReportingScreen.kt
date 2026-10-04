package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCommodity
import com.example.data.model.PackagingMaterial
import com.example.data.model.RecommendationResult
import com.example.ui.components.LcaComparisonCard
import com.example.ui.components.TradeOffMatrixChart

@Composable
fun AnalyticalReportingScreen(
    commodity: FoodCommodity?,
    recommendations: List<RecommendationResult>,
    materials: List<PackagingMaterial>,
    selectedRecommendation: RecommendationResult?,
    onSelectRecommendation: (RecommendationResult) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Trade-Off Matrix (Quadrant Chart)
        item {
            TradeOffMatrixChart(
                recommendations = recommendations,
                onSelectMaterial = onSelectRecommendation
            )
        }

        // Section 2: Life Cycle Assessment (LCA) Comparison
        item {
            LcaComparisonCard(
                currentMaterial = selectedRecommendation?.material ?: materials.firstOrNull()
            )
        }

        // Section 3: Material Permeability & Technical Comparison Table
        item {
            MaterialPropertiesTableCard(materials = materials)
        }

        // Section 4: Exportable Supplier Technical Specification Report
        item {
            if (commodity != null && selectedRecommendation != null) {
                val reportText = generateSupplierSpecSheetText(commodity, selectedRecommendation)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Technical Spec Sheet & Bill of Materials",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Export-ready supplier compliance report",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Report Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = reportText,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Copy / Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(reportText))
                                    onShowMessage("Spec sheet copied to clipboard!")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Copy Spec", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    shareReport(context, reportText)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Share Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialPropertiesTableCard(
    materials: List<PackagingMaterial>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Barrier Permeability & Properties Catalog",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "OTR (cc/m²/d) • WVTR (g/m²/d) • Carbon (kg CO₂/kg)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontally Scrollable Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.width(580.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Material", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(140.dp))
                        Text(text = "OTR", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp))
                        Text(text = "WVTR", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(75.dp))
                        Text(text = "CO₂ kg", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp))
                        Text(text = "Degrade", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                        Text(text = "Circularity", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    materials.forEachIndexed { idx, mat ->
                        val bg = if (idx % 2 == 0) Color.Transparent else Color(0xFFF8FAFC)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(140.dp)) {
                                Text(text = mat.shortCode, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = mat.category, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                            }
                            Text(text = "${mat.otr.toInt()}", fontSize = 11.sp, modifier = Modifier.width(75.dp))
                            Text(text = "${mat.wvtr.toInt()}", fontSize = 11.sp, modifier = Modifier.width(75.dp))
                            Text(text = "${mat.carbonFootprintKgCo2}", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                            Text(text = "~${mat.degradationDays}d", fontSize = 11.sp, modifier = Modifier.width(90.dp))
                            Text(text = "${mat.circularityScore}/100", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun generateSupplierSpecSheetText(
    commodity: FoodCommodity,
    rec: RecommendationResult
): String {
    return """
============================================================
           ECOPACK AI TECHNICAL SPECIFICATION SHEET
============================================================
Date Generated    : 2026-10-04
Project ID        : SPEC-${commodity.category.take(3).uppercase()}-${rec.material.shortCode}
Status            : APPROVED FOR COMMERCIAL PROTOTYPE

[ 1. COMMODITY REQUIREMENTS ]
Commodity         : ${commodity.name} (${commodity.category})
Baseline Life     : ${commodity.baselineShelfLifeDays} days @ ${commodity.idealStorageTemp}
Primary Spoilage  : ${commodity.primarySpoilageFactor}
Target Barrier    : OTR ${commodity.targetOtrMin}-${commodity.targetOtrMax} cc | WVTR ${commodity.targetWvtrMin}-${commodity.targetWvtrMax} g

[ 2. SELECTED PACKAGING MATERIAL ]
Resin / Substrate : ${rec.material.name}
Grade Shortcode   : ${rec.material.shortCode}
Classification    : ${rec.material.category}
Permeability      : OTR: ${rec.material.otr} cc/m²/d/atm | WVTR: ${rec.material.wvtr} g/m²/d
Mechanical Specs  : Tensile Strength ${rec.material.tensileStrengthMpa} MPa
Temperature Range : ${rec.material.minTempCelsius}°C to ${rec.material.maxTempCelsius}°C

[ 3. PERFORMANCE & ENVIRONMENTAL IMPACT ]
Predicted Shelf   : ${rec.predictedShelfLifeDays} days (+${rec.shelfLifeExtensionPercent.toInt()}% Freshness Gain)
Eco-Score Rating  : ${rec.environmentalImpactScore}/100 (Optimal Circular Tier)
Embodied Carbon   : ${rec.material.carbonFootprintKgCo2} kg CO2e / kg material
Carbon Avoided    : ${rec.carbonSavingsKgPerTon.toInt()} kg CO2e / metric ton vs Virgin Fossil
End-of-Life Route : ${rec.material.endOfLife} (~${rec.material.degradationDays} days)
Circularity Score : ${rec.material.circularityScore}/100

[ 4. REGULATORY & FOOD CONTACT COMPLIANCE ]
Certifications    : ${rec.material.certifications}
Additives Required: ${rec.suggestedActiveAdditives}
============================================================
    """.trimIndent()
}

private fun shareReport(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Supplier Specification Sheet")
    context.startActivity(shareIntent)
}
