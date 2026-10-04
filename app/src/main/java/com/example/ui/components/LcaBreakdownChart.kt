package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PackagingMaterial

data class LcaStageEmission(
    val stageName: String,
    val emissionsKg: Double,
    val color: Color
)

@Composable
fun LcaComparisonCard(
    currentMaterial: PackagingMaterial?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Life Cycle Assessment (LCA) Carbon Benchmark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Global Warming Potential (kg CO₂-eq / kg packaging material)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Compare Virgin Plastic baseline vs selected vs alternatives
            val baselineKg = 4.5
            val currentKg = currentMaterial?.carbonFootprintKgCo2 ?: 1.1

            LcaMaterialBar(
                name = currentMaterial?.name ?: "Selected Bio-Material",
                tag = currentMaterial?.endOfLife ?: "Compostable",
                carbonKg = currentKg,
                maxCarbonKg = 5.0,
                barColor = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LcaMaterialBar(
                name = "100% rPET Bottle-to-Bottle Resin",
                tag = "Recycled Polymer",
                carbonKg = 1.45,
                maxCarbonKg = 5.0,
                barColor = Color(0xFF0284C7)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LcaMaterialBar(
                name = "Molded Sugarcane Bagasse Agro-Fiber",
                tag = "Agricultural Waste",
                carbonKg = 0.45,
                maxCarbonKg = 5.0,
                barColor = Color(0xFF059669)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LcaMaterialBar(
                name = "Virgin Fossil Polymer Baseline (PET / PS / Alu)",
                tag = "Standard Linear Economy",
                carbonKg = baselineKg,
                maxCarbonKg = 5.0,
                barColor = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Carbon Mitigation Callout
            val savingsPct = (((baselineKg - currentKg) / baselineKg) * 100.0).toInt().coerceAtLeast(0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFECFDF5))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "-$savingsPct%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "$savingsPct% Carbon Footprint Reduction",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Saves approx ${(baselineKg - currentKg) * 1000} kg CO₂e per metric ton compared to conventional petroleum plastic.",
                        color = Color(0xFF047857),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LcaMaterialBar(
    name: String,
    tag: String,
    carbonKg: Double,
    maxCarbonKg: Double,
    barColor: Color
) {
    val progress = (carbonKg / maxCarbonKg).toFloat().coerceIn(0.05f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = tag,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "$carbonKg kg CO₂",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
