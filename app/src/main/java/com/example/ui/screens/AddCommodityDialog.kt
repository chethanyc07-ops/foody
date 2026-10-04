package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommodityDialog(
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val categories = listOf("Fresh Produce", "Meat & Poultry", "Seafood", "Bakery", "Dairy", "Dry Staples")
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var baselineShelfLife by remember { mutableIntStateOf(5) }
    var idealTemp by remember { mutableStateOf("2°C - 4°C (Chilled)") }
    var respiration by remember { mutableStateOf("Medium") }
    var moistureSens by remember { mutableIntStateOf(4) }
    var oxygenSens by remember { mutableIntStateOf(4) }
    var spoilageFactor by remember { mutableStateOf("Moisture migration & aerobic oxidation") }
    var otrMin by remember { mutableDoubleStateOf(100.0) }
    var otrMax by remember { mutableDoubleStateOf(1500.0) }
    var wvtrMin by remember { mutableDoubleStateOf(5.0) }
    var wvtrMax by remember { mutableDoubleStateOf(25.0) }
    var emoji by remember { mutableStateOf("🥑") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Food Commodity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(2) },
                        label = { Text("Icon") },
                        modifier = Modifier.width(70.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Commodity Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Baseline shelf life slider
                Text(
                    text = "Baseline Shelf Life: $baselineShelfLife days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = baselineShelfLife.toFloat(),
                    onValueChange = { baselineShelfLife = it.toInt() },
                    valueRange = 1f..60f
                )

                OutlinedTextField(
                    value = idealTemp,
                    onValueChange = { idealTemp = it },
                    label = { Text("Storage Condition / Temp") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = spoilageFactor,
                    onValueChange = { spoilageFactor = it },
                    label = { Text("Primary Spoilage Factor") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Sensitivity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "O₂ Sens: $oxygenSens/5", fontSize = 11.sp)
                        Slider(
                            value = oxygenSens.toFloat(),
                            onValueChange = { oxygenSens = it.toInt() },
                            valueRange = 1f..5f
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "H₂O Sens: $moistureSens/5", fontSize = 11.sp)
                        Slider(
                            value = moistureSens.toFloat(),
                            onValueChange = { moistureSens = it.toInt() },
                            valueRange = 1f..5f
                        )
                    }
                }

                // Target OTR and WVTR ranges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = otrMin.toString(),
                        onValueChange = { otrMin = it.toDoubleOrNull() ?: 1.0 },
                        label = { Text("OTR Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = otrMax.toString(),
                        onValueChange = { otrMax = it.toDoubleOrNull() ?: 1000.0 },
                        label = { Text("OTR Max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wvtrMin.toString(),
                        onValueChange = { wvtrMin = it.toDoubleOrNull() ?: 1.0 },
                        label = { Text("WVTR Min") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = wvtrMax.toString(),
                        onValueChange = { wvtrMax = it.toDoubleOrNull() ?: 50.0 },
                        label = { Text("WVTR Max") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name, selectedCategory, baselineShelfLife, idealTemp,
                            respiration, moistureSens, oxygenSens, spoilageFactor,
                            otrMin, otrMax, wvtrMin, wvtrMax, emoji
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Add Commodity")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
