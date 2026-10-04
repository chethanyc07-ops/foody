package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecommendationResult
import kotlin.math.sqrt

@Composable
fun TradeOffMatrixChart(
    recommendations: List<RecommendationResult>,
    onSelectMaterial: (RecommendationResult) -> Unit,
    modifier: Modifier = Modifier
) {
    var highlightedIndex by remember { mutableStateOf<Int?>(0) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trade-Off Matrix: Freshness vs Eco-Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Quadrant analysis for multi-objective material selection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Matrix Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .pointerInput(recommendations) {
                            detectTapGestures { tapOffset ->
                                val w = size.width
                                val h = size.height
                                var closestIndex: Int? = null
                                var minDistance = Float.MAX_VALUE

                                recommendations.forEachIndexed { idx, item ->
                                    val x = (item.environmentalImpactScore / 100f) * w
                                    val maxExt = 200f
                                    val y = h - ((item.shelfLifeExtensionPercent.toFloat().coerceIn(0f, maxExt) / maxExt) * h)
                                    val dist = sqrt((tapOffset.x - x) * (tapOffset.x - x) + (tapOffset.y - y) * (tapOffset.y - y))
                                    if (dist < 35f && dist < minDistance) {
                                        minDistance = dist
                                        closestIndex = idx
                                    }
                                }

                                closestIndex?.let {
                                    highlightedIndex = it
                                    onSelectMaterial(recommendations[it])
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val midX = w / 2f
                    val midY = h / 2f

                    // Quadrant Background Tints
                    // Top-Right: Ideal (High Eco + High Shelf life)
                    drawRect(
                        color = Color(0x2210B981),
                        topLeft = Offset(midX, 0f),
                        size = androidx.compose.ui.geometry.Size(midX, midY)
                    )

                    // Top-Left: High Shelf life, Low Eco
                    drawRect(
                        color = Color(0x153B82F6),
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(midX, midY)
                    )

                    // Bottom-Right: Low Shelf life, High Eco
                    drawRect(
                        color = Color(0x15F59E0B),
                        topLeft = Offset(midX, midY),
                        size = androidx.compose.ui.geometry.Size(midX, midY)
                    )

                    // Axes lines
                    val dashed = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 2f,
                        pathEffect = dashed
                    )
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(midX, 0f),
                        end = Offset(midX, h),
                        strokeWidth = 2f,
                        pathEffect = dashed
                    )

                    // Plot Points
                    recommendations.forEachIndexed { index, item ->
                        val isSelected = highlightedIndex == index
                        val x = (item.environmentalImpactScore / 100f) * w
                        val maxExt = 200f
                        val y = h - ((item.shelfLifeExtensionPercent.toFloat().coerceIn(0f, maxExt) / maxExt) * h)

                        val pointColor = when {
                            item.environmentalImpactScore >= 85 && item.shelfLifeExtensionPercent >= 70.0 -> Color(0xFF059669) // Top Green
                            item.shelfLifeExtensionPercent >= 70.0 -> Color(0xFF2563EB) // Barrier Focus
                            item.environmentalImpactScore >= 85 -> Color(0xFFD97706) // Eco Focus
                            else -> Color(0xFF64748B)
                        }

                        // Outer ring if selected
                        if (isSelected) {
                            drawCircle(
                                color = pointColor.copy(alpha = 0.35f),
                                radius = 18f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = pointColor,
                                radius = 10f,
                                center = Offset(x, y),
                                style = Stroke(width = 3f)
                            )
                        }

                        drawCircle(
                            color = pointColor,
                            radius = if (isSelected) 7f else 6f,
                            center = Offset(x, y)
                        )
                    }
                }

                // Quadrant Labels
                Text(
                    text = "🌿 High Eco / High Freshness (Optimal)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                Text(
                    text = "⚡ Barrier Heavyweight",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D4ED8),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                Text(
                    text = "🌱 Ultra-Green / Moderate Life",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB45309),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )

                Text(
                    text = "⚠️ Sub-Optimal Balance",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Axis Explanations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "X-Axis: Environmental Score (0-100)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Y-Axis: Shelf Life Extension (+%)",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Highlighted Item Summary Banner
            highlightedIndex?.let { idx ->
                if (idx < recommendations.size) {
                    val sel = recommendations[idx]
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .clickable { onSelectMaterial(sel) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${sel.material.name} (${sel.material.shortCode})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Freshness: +${sel.shelfLifeExtensionPercent.toInt()}% (${sel.predictedShelfLifeDays} days) • Eco-Score: ${sel.environmentalImpactScore}/100",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Inspect",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
