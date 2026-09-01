package com.example.foodlogger.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodlogger.data.model.DailyNutritionTarget
import com.example.foodlogger.data.model.NutrientDetails
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary

enum class NutrientStatus(val label: String, val color: Color) {
    APPROPRIATE("適正", Color(0xFF4CAF50)),
    INSUFFICIENT("不足", Color(0xFF29B6F6)),
    EXCESSIVE("過剰", Color(0xFFE53935))
}

data class NutrientBarItem(
    val name: String,
    val actualValue: Double,
    val targetValue: Double,
    val unit: String,
    val status: NutrientStatus,
    val isUpperLimitOnly: Boolean = false // e.g. Salt
)

@Composable
fun NutritionBarChartSection(
    nutrients: NutrientDetails,
    targets: DailyNutritionTarget,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        createNutrientItem("エネルギー", nutrients.caloriesKcal, targets.targetCaloriesKcal, "kcal", canBeExcessive = true, lowRatio = 0.85, highRatio = 1.15),
        createNutrientItem("たんぱく質", nutrients.proteinG, targets.targetProteinG, "g", canBeExcessive = false, lowRatio = 0.85),
        createNutrientItem("脂質", nutrients.fatG, targets.targetFatG, "g", canBeExcessive = true, lowRatio = 0.80, highRatio = 1.25),
        createNutrientItem("炭水化物", nutrients.carbohydrateG, targets.targetCarbsG, "g", canBeExcessive = true, lowRatio = 0.80, highRatio = 1.25),
        createNutrientItem("カルシウム", nutrients.calciumMg, targets.targetCalciumMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("鉄", nutrients.ironMg, targets.targetIronMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("亜鉛", nutrients.zincMg, targets.targetZincMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("マグネシウム", nutrients.magnesiumMg, targets.targetMagnesiumMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンA", nutrients.vitaminAMcg, targets.targetVitaminAMcg, "µg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンB1", nutrients.vitaminB1Mg, 1.2, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンB2", nutrients.vitaminB2Mg, 1.4, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンC", nutrients.vitaminCMg, targets.targetVitaminCMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンD", nutrients.vitaminDMcg, targets.targetVitaminDMcg, "µg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("ビタミンE", nutrients.vitaminEMg, targets.targetVitaminEMg, "mg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("葉酸", nutrients.folateMcg, targets.targetFolateMcg, "µg", canBeExcessive = false, lowRatio = 0.80),
        createNutrientItem("食物繊維", nutrients.fiberG, targets.targetFiberG, "g", canBeExcessive = false, lowRatio = 0.85),
        createSaltItem("塩分", nutrients.saltEquivalentG, targets.maxSaltG, "g")
    )

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(nutrients) {
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "栄養素バランス（1日の基準比較）",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = Color(0xFF8BC34A),
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
                Text("摂取量", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(2.dp),
                    color = Color(0xFFDCEDC8),
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
                Text("適正ゾーン", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()

            // Header labels for graph
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "栄養素",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = "判定",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(38.dp)
                )
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "基準値",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "摂取量",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(60.dp)
                )
            }

            // Items List
            items.forEach { item ->
                NutrientBarRow(
                    item = item,
                    animatedProgress = animatedProgress.value
                )
            }
        }
    }
}

@Composable
private fun NutrientBarRow(
    item: NutrientBarItem,
    animatedProgress: Float
) {
    // We set 100% target at 55% of the chart width
    // max display is 180% of target
    val targetRatio = if (item.targetValue > 0) (item.actualValue / item.targetValue).toFloat() else 0f
    val currentRatio = targetRatio * animatedProgress

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nutrient Name
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(64.dp)
        )

        // Status Badge (適正, 不足, 過剰)
        Surface(
            shape = RoundedCornerShape(3.dp),
            color = item.status.color,
            modifier = Modifier.width(36.dp)
        ) {
            Text(
                text = item.status.label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Bar Chart Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
                val width = size.width
                val height = size.height

                // Target position (100%) at 55% width
                val targetX = width * 0.55f
                val zoneStartX = targetX * 0.80f
                val zoneEndX = if (item.isUpperLimitOnly) targetX else targetX * 1.25f

                // Draw Ideal Zone (Soft Green Background)
                drawRect(
                    color = Color(0xFFDCEDC8).copy(alpha = 0.8f),
                    topLeft = Offset(if (item.isUpperLimitOnly) 0f else zoneStartX, 0f),
                    size = Size(if (item.isUpperLimitOnly) targetX else (zoneEndX - zoneStartX), height)
                )

                // Draw Target Center Dotted Line
                drawLine(
                    color = Color(0xFF9E9E9E),
                    start = Offset(targetX, 0f),
                    end = Offset(targetX, height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Draw Actual Intake Green Bar
                val barWidth = (targetX * currentRatio).coerceIn(0f, width)
                val barColor = when (item.status) {
                    NutrientStatus.EXCESSIVE -> Color(0xFF8BC34A)
                    NutrientStatus.INSUFFICIENT -> Color(0xFF8BC34A)
                    NutrientStatus.APPROPRIATE -> Color(0xFF8BC34A)
                }

                if (barWidth > 0) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(0f, 1.dp.toPx()),
                        size = Size(barWidth, height - 2.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Actual Value Text
        val valueText = if (item.actualValue >= 100) {
            "${item.actualValue.toInt()}${item.unit}"
        } else if (item.actualValue >= 10) {
            "${String.format("%.1f", item.actualValue)}${item.unit}"
        } else {
            "${String.format("%.2f", item.actualValue)}${item.unit}"
        }

        Text(
            text = valueText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(60.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

private fun createNutrientItem(
    name: String,
    actual: Double,
    target: Double,
    unit: String,
    canBeExcessive: Boolean,
    lowRatio: Double = 0.80,
    highRatio: Double = 1.20
): NutrientBarItem {
    val ratio = if (target > 0) actual / target else 1.0
    val status = when {
        ratio < lowRatio -> NutrientStatus.INSUFFICIENT
        canBeExcessive && ratio > highRatio -> NutrientStatus.EXCESSIVE
        else -> NutrientStatus.APPROPRIATE // Positive assessment for vitamins/fiber/minerals exceeding target
    }
    return NutrientBarItem(name, actual, target, unit, status)
}

private fun createSaltItem(
    name: String,
    actual: Double,
    maxLimit: Double,
    unit: String
): NutrientBarItem {
    val status = if (actual <= maxLimit) NutrientStatus.APPROPRIATE else NutrientStatus.EXCESSIVE
    return NutrientBarItem(name, actual, maxLimit, unit, status, isUpperLimitOnly = true)
}
