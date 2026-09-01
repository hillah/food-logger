package com.example.foodlogger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.example.foodlogger.ui.components.PfcMacroSection
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary

@Composable
fun NutritionPreviewCard(
    result: NutritionAnalysisResult,
    onResultChanged: (NutritionAnalysisResult) -> Unit,
    onSaveToHealthConnect: (NutritionAnalysisResult) -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMicroNutrients by remember { mutableStateOf(false) }
    var hasModifiedMealName by remember { mutableStateOf(false) }

    val mealCategoryLabel = when (result.mealType.uppercase()) {
        "BREAKFAST" -> "朝食"
        "LUNCH" -> "昼食"
        "DINNER" -> "夕食"
        "SNACK" -> "間食"
        else -> "その他"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Status Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = EmeraldGreenPrimary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Health Connect に記録しました！",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreenPrimary
                            )
                            Text(
                                text = "食事名の修正や栄養バランスを確認できます",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Fixed Meal Category Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldGreenPrimary
                    ) {
                        Text(
                            text = mealCategoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // AI Notes / Advice Card (Prominent display)
            if (result.notes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI 栄養アドバイス・特記事項",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = result.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Editable Meal Name
            OutlinedTextField(
                value = result.mealName,
                onValueChange = {
                    hasModifiedMealName = true
                    onResultChanged(result.copy(mealName = it))
                },
                label = { Text("食事名（編集可能）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // PFC Macro Chart with Ideal Comparison
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

            // Read-Only Nutrients List
            Text(
                text = "栄養素の内訳",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            ReadOnlyNutrientRow(label = "エネルギー (カロリー)", value = "${result.nutrients.caloriesKcal.toInt()} kcal")
            ReadOnlyNutrientRow(label = "タンパク質 (P)", value = "${String.format("%.1f", result.nutrients.proteinG)} g")
            ReadOnlyNutrientRow(label = "脂質 (F)", value = "${String.format("%.1f", result.nutrients.fatG)} g")
            ReadOnlyNutrientRow(label = "炭水化物 (C)", value = "${String.format("%.1f", result.nutrients.carbohydrateG)} g")
            ReadOnlyNutrientRow(label = "食物繊維", value = "${String.format("%.1f", result.nutrients.fiberG)} g")
            ReadOnlyNutrientRow(label = "食塩相当量", value = "${String.format("%.1f", result.nutrients.saltEquivalentG)} g")

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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReadOnlyNutrientRow(label = "カルシウム", value = "${result.nutrients.calciumMg.toInt()} mg")
                    ReadOnlyNutrientRow(label = "鉄分", value = "${String.format("%.1f", result.nutrients.ironMg)} mg")
                    ReadOnlyNutrientRow(label = "亜鉛", value = "${String.format("%.1f", result.nutrients.zincMg)} mg")
                    ReadOnlyNutrientRow(label = "マグネシウム", value = "${result.nutrients.magnesiumMg.toInt()} mg")
                    ReadOnlyNutrientRow(label = "ビタミンA", value = "${result.nutrients.vitaminAMcg.toInt()} µg")
                    ReadOnlyNutrientRow(label = "ビタミンC", value = "${result.nutrients.vitaminCMg.toInt()} mg")
                    ReadOnlyNutrientRow(label = "ビタミンD", value = "${String.format("%.1f", result.nutrients.vitaminDMcg)} µg")
                    ReadOnlyNutrientRow(label = "ビタミンE", value = "${String.format("%.1f", result.nutrients.vitaminEMg)} mg")
                    ReadOnlyNutrientRow(label = "葉酸", value = "${result.nutrients.folateMcg.toInt()} µg")
                }
            }

            // Return to Dashboard Button (Primary)
            Button(
                onClick = {
                    if (hasModifiedMealName) {
                        onSaveToHealthConnect(result)
                    } else {
                        onBackToDashboard()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasModifiedMealName) "食事名を更新してダッシュボードへ戻る" else "確認完了（ダッシュボードへ戻る）",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyNutrientRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
