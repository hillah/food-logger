package com.example.foodlogger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodlogger.data.model.DailyNutritionTarget
import com.example.foodlogger.data.model.EvaluationGrade
import com.example.foodlogger.data.model.NutrientDetails
import com.example.foodlogger.data.model.NutritionStandards
import com.example.foodlogger.ui.components.PfcMacroSection
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailySummaryScreen(
    targetDate: LocalDate,
    dailyTotalNutrients: NutrientDetails,
    hasCompletedMainMeals: Boolean,
    ageGroup: String,
    gender: String,
    activityLevel: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日(E)", Locale.JAPANESE)
    val standards = NutritionStandards.getDailyTarget(ageGroup, gender, activityLevel)

    val pKcal = dailyTotalNutrients.proteinG * 4.0
    val fKcal = dailyTotalNutrients.fatG * 9.0
    val cKcal = dailyTotalNutrients.carbohydrateG * 4.0
    val totalMacroKcal = pKcal + fKcal + cKcal

    val pPct = if (totalMacroKcal > 0) (pKcal / totalMacroKcal * 100).toInt() else 0
    val fPct = if (totalMacroKcal > 0) (fKcal / totalMacroKcal * 100).toInt() else 0
    val cPct = if (totalMacroKcal > 0) (cKcal / totalMacroKcal * 100).toInt() else 0

    val evaluation = NutritionStandards.evaluateDailyIntake(
        hasCompletedMainMeals = hasCompletedMainMeals,
        totalCalories = dailyTotalNutrients.caloriesKcal,
        targetCalories = standards.targetCaloriesKcal,
        pPct = pPct,
        fPct = fPct,
        cPct = cPct
    )

    val calorieProgress = (dailyTotalNutrients.caloriesKcal / standards.targetCaloriesKcal).toFloat().coerceIn(0f, 1.5f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldGreenPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = targetDate.format(dateFormatter),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "日次栄養総括",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // User Profile Target Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${standards.ageGroupLabel} ${standards.genderLabel} (${standards.activityLevelLabel}) 基準",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Overall Evaluation Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1日の総合判定",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = evaluation.badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = evaluation.label,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = evaluation.badgeColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Calorie Comparison Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1日の総摂取エネルギー",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(calorieProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (calorieProgress in 0.85f..1.15f) EmeraldGreenPrimary else MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = { (calorieProgress / 1.5f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (calorieProgress in 0.85f..1.15f) EmeraldGreenPrimary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "摂取量: ${dailyTotalNutrients.caloriesKcal.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "目安基準: ${standards.targetCaloriesKcal.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1-Day PFC Macro Balance Section
        PfcMacroSection(
            calories = dailyTotalNutrients.caloriesKcal,
            proteinG = dailyTotalNutrients.proteinG,
            fatG = dailyTotalNutrients.fatG,
            carbsG = dailyTotalNutrients.carbohydrateG
        )

        // Asken-Style Detailed Nutrition Bar Chart Section
        com.example.foodlogger.ui.components.NutritionBarChartSection(
            nutrients = dailyTotalNutrients,
            targets = standards
        )

        // Back to Dashboard Button
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("ダッシュボードへ戻る", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

enum class ComparisonStatus(val label: String, val color: Color) {
    APPROPRIATE("適正", EmeraldGreenPrimary),
    HIGH("多め", Color(0xFFE65100)),
    LOW("少なめ", Color(0xFF1976D2))
}

private fun getStatus(actual: Double, target: Double, lowRatio: Double, highRatio: Double): ComparisonStatus {
    if (target <= 0) return ComparisonStatus.APPROPRIATE
    val ratio = actual / target
    return when {
        ratio < lowRatio -> ComparisonStatus.LOW
        ratio > highRatio -> ComparisonStatus.HIGH
        else -> ComparisonStatus.APPROPRIATE
    }
}

@Composable
private fun NutrientComparisonRow(
    name: String,
    actual: String,
    target: String,
    status: ComparisonStatus
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.3f))
        Text(text = actual, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1.1f))
        Text(text = target, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.1f))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = status.color.copy(alpha = 0.15f),
            modifier = Modifier.weight(0.9f)
        ) {
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                color = status.color,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
