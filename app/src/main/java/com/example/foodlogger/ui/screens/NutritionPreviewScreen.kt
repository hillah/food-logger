package com.example.foodlogger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodlogger.data.model.NutritionAnalysisResult
import com.example.foodlogger.ui.components.EditableNutrientItem
import com.example.foodlogger.ui.components.PfcMacroSection
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary

@Composable
fun NutritionPreviewCard(
    result: NutritionAnalysisResult,
    onResultChanged: (NutritionAnalysisResult) -> Unit,
    onSaveToHealthConnect: (NutritionAnalysisResult) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMicroNutrients by remember { mutableStateOf(false) }

    val mealTypes = listOf("BREAKFAST" to "朝食", "LUNCH" to "昼食", "DINNER" to "夕食", "SNACK" to "間食")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title and Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = EmeraldGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "解析結果・微調整",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("破棄", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Editable Meal Name
            OutlinedTextField(
                value = result.mealName,
                onValueChange = { onResultChanged(result.copy(mealName = it)) },
                label = { Text("食事名") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Meal Type Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEach { (typeKey, typeLabel) ->
                    FilterChip(
                        selected = result.mealType.equals(typeKey, ignoreCase = true),
                        onClick = { onResultChanged(result.copy(mealType = typeKey)) },
                        label = { Text(typeLabel) }
                    )
                }
            }

            // PFC Macro Chart
            PfcMacroSection(
                calories = result.nutrients.caloriesKcal,
                proteinG = result.nutrients.proteinG,
                fatG = result.nutrients.fatG,
                carbsG = result.nutrients.carbohydrateG
            )

            // Dish breakdowns if available
            if (result.dishes.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "内訳品目:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    result.dishes.forEach { dish ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("・${dish.name} (${dish.estimatedPortion})", style = MaterialTheme.typography.bodySmall)
                            if (dish.caloriesKcal > 0) {
                                Text("${dish.caloriesKcal.toInt()} kcal", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Macro Nutrients Quick Edit
            Text(
                text = "主要栄養素の微調整",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            EditableNutrientItem(
                label = "エネルギー (カロリー)",
                value = result.nutrients.caloriesKcal,
                unit = "kcal",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(caloriesKcal = it))) }
            )

            EditableNutrientItem(
                label = "タンパク質 (P)",
                value = result.nutrients.proteinG,
                unit = "g",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(proteinG = it))) }
            )

            EditableNutrientItem(
                label = "脂質 (F)",
                value = result.nutrients.fatG,
                unit = "g",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(fatG = it))) }
            )

            EditableNutrientItem(
                label = "炭水化物 (C)",
                value = result.nutrients.carbohydrateG,
                unit = "g",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(carbohydrateG = it))) }
            )

            EditableNutrientItem(
                label = "食物繊維",
                value = result.nutrients.fiberG,
                unit = "g",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(fiberG = it))) }
            )

            EditableNutrientItem(
                label = "食塩相当量",
                value = result.nutrients.saltEquivalentG,
                unit = "g",
                onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(saltEquivalentG = it))) }
            )

            // Micronutrients Accordion Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ビタミン・ミネラル詳細 (${if (showMicroNutrients) "閉じる" else "展開"})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { showMicroNutrients = !showMicroNutrients }) {
                    Icon(
                        imageVector = if (showMicroNutrients) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle micronutrients"
                    )
                }
            }

            AnimatedVisibility(visible = showMicroNutrients) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditableNutrientItem(
                        label = "カルシウム",
                        value = result.nutrients.calciumMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(calciumMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "鉄分",
                        value = result.nutrients.ironMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(ironMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "亜鉛",
                        value = result.nutrients.zincMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(zincMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "マグネシウム",
                        value = result.nutrients.magnesiumMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(magnesiumMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "ビタミンA",
                        value = result.nutrients.vitaminAMcg,
                        unit = "µg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(vitaminAMcg = it))) }
                    )
                    EditableNutrientItem(
                        label = "ビタミンC",
                        value = result.nutrients.vitaminCMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(vitaminCMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "ビタミンD",
                        value = result.nutrients.vitaminDMcg,
                        unit = "µg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(vitaminDMcg = it))) }
                    )
                    EditableNutrientItem(
                        label = "ビタミンE",
                        value = result.nutrients.vitaminEMg,
                        unit = "mg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(vitaminEMg = it))) }
                    )
                    EditableNutrientItem(
                        label = "葉酸",
                        value = result.nutrients.folateMcg,
                        unit = "µg",
                        onValueChange = { onResultChanged(result.copy(nutrients = result.nutrients.copy(folateMcg = it))) }
                    )
                }
            }

            if (result.notes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "💡 ${result.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Health Connect Register Button
            Button(
                onClick = { onSaveToHealthConnect(result) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Connect に記録する",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
