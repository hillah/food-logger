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
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.health.connect.client.records.NutritionRecord
import com.example.foodlogger.data.model.DailyNutritionTarget
import com.example.foodlogger.data.model.EvaluationGrade
import com.example.foodlogger.data.model.NutrientDetails
import com.example.foodlogger.data.model.NutritionStandards
import com.example.foodlogger.ui.components.PfcMacroSection
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary
import com.example.foodlogger.viewmodel.DailyAdviceUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailySummaryScreen(
    targetDate: LocalDate,
    dailyTotalNutrients: NutrientDetails,
    hasCompletedMainMeals: Boolean,
    dayRecords: Map<Int, List<NutritionRecord>> = emptyMap(),
    ageGroup: String,
    gender: String,
    activityLevel: String,
    dailyAdviceState: DailyAdviceUiState = DailyAdviceUiState.Idle,
    onRequestAdvice: () -> Unit = {},
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

        // Recorded Meals Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = EmeraldGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "記録された食事内容",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                val categoriesWithRecords = MealCategory.values().map { category ->
                    category to (dayRecords[category.mealTypeConstant] ?: emptyList())
                }

                val hasAnyRecord = categoriesWithRecords.any { it.second.isNotEmpty() }

                if (!hasAnyRecord) {
                    Text(
                        text = "この日の食事記録はありません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categoriesWithRecords.forEach { (category, records) ->
                            MealSummaryCategoryRow(category = category, records = records)
                        }
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

        // AI Nutritionist Advice Card Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EmeraldGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "管理栄養士のAIアドバイス",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (dailyAdviceState is DailyAdviceUiState.Success) {
                        OutlinedButton(
                            onClick = onRequestAdvice,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("再分析", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                HorizontalDivider()

                when (dailyAdviceState) {
                    is DailyAdviceUiState.Idle -> {
                        Text(
                            text = "本日の食事内容・総摂取栄養素・PFCバランスをもとに、プロの管理栄養士AIによる詳しい評価と改善アドバイスを生成します。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onRequestAdvice,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreenPrimary)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AIアドバイスを取得する",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    is DailyAdviceUiState.Loading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = EmeraldGreenPrimary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "管理栄養士AIが食事内容と栄養バランスを分析中...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is DailyAdviceUiState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FormattedNutritionAdviceView(
                                adviceText = dailyAdviceState.advice,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                    is DailyAdviceUiState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "アドバイス取得エラー",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Text(
                                    text = dailyAdviceState.errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                FilledTonalButton(
                                    onClick = onRequestAdvice,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("再試行")
                                }
                            }
                        }
                    }
                }
            }
        }

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
private fun MealSummaryCategoryRow(
    category: MealCategory,
    records: List<NutritionRecord>
) {
    if (records.isEmpty()) {
        SingleMealItemView(
            categoryLabel = category.label,
            title = "未登録",
            calorieText = "ー",
            isSkipped = false,
            isRegistered = false
        )
        return
    }

    if (records.size == 1) {
        val record = records.first()
        val isSkipped = record.name == "食事なし" || (record.energy?.inKilocalories ?: 0.0) == 0.0
        SingleMealItemView(
            categoryLabel = category.label,
            title = if (isSkipped) "食事なし（欠食）" else (record.name?.ifBlank { "食事記録" } ?: "食事記録"),
            calorieText = if (isSkipped) "0 kcal" else "${(record.energy?.inKilocalories ?: 0.0).toInt()} kcal",
            isSkipped = isSkipped,
            isRegistered = !isSkipped
        )
        return
    }

    // Multiple records in this category (e.g. multiple entries for OTHER or SNACK)
    val totalCalories = records.sumOf { it.energy?.inKilocalories ?: 0.0 }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldGreenPrimary.copy(alpha = 0.18f),
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = EmeraldGreenPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${records.size}件の記録",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "計 ${totalCalories.toInt()} kcal",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = EmeraldGreenPrimary
            )
        }

        // Sub-items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            records.forEach { rec ->
                val kcal = rec.energy?.inKilocalories ?: 0.0
                val name = rec.name?.ifBlank { "食事記録" } ?: "食事記録"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "・$name",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${kcal.toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleMealItemView(
    categoryLabel: String,
    title: String,
    calorieText: String,
    isSkipped: Boolean,
    isRegistered: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isRegistered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    isSkipped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = when {
                isRegistered -> EmeraldGreenPrimary.copy(alpha = 0.18f)
                isSkipped -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.width(52.dp)
        ) {
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = when {
                    isRegistered -> EmeraldGreenPrimary
                    isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Meal Name / Status
        Column(modifier = Modifier.weight(1f)) {
            if (isSkipped) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoNotDisturbOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = title,
                    style = if (isRegistered) {
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    } else {
                        MaterialTheme.typography.bodySmall
                    },
                    color = if (isRegistered) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }

        // Calories
        Text(
            text = calorieText,
            style = if (isRegistered) {
                MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.labelSmall
            },
            color = when {
                isRegistered -> EmeraldGreenPrimary
                isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
        )
    }
}

@Composable
fun FormattedNutritionAdviceView(
    adviceText: String,
    modifier: Modifier = Modifier
) {
    val lines = adviceText.lines()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()

            when {
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Section Header 1 or 2 (# Title, ## Title)
                line.startsWith("## ") || line.startsWith("# ") -> {
                    val title = line.removePrefix("## ").removePrefix("# ").trim().replace("**", "")
                    AdviceSectionHeader(title = title)
                }

                // Section Header 3 or Numbered Big Header (### Title, 1. **Title**, **1. Title**, 【Title】)
                line.startsWith("### ") || isAdviceSectionHeader(line) -> {
                    val cleanTitle = line.removePrefix("### ").trim()
                    AdviceSubSectionHeader(title = cleanTitle)
                }

                // Bullet points / List Items (- Item, * Item, ・Item, 1. Item)
                line.startsWith("- ") || line.startsWith("* ") || line.startsWith("・") || isNumberedListItem(line) -> {
                    val content = when {
                        line.startsWith("- ") -> line.removePrefix("- ")
                        line.startsWith("* ") -> line.removePrefix("* ")
                        line.startsWith("・") -> line.removePrefix("・")
                        else -> line.replaceFirst(Regex("""^\d+\.\s*"""), "")
                    }.trim()

                    AdviceBulletItem(content = content)
                }

                // Normal Paragraph
                else -> {
                    Text(
                        text = parseMarkdownToAnnotatedString(
                            text = line,
                            baseColor = MaterialTheme.colorScheme.onSurface
                        ),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AdviceSectionHeader(title: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldGreenPrimary.copy(alpha = 0.15f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EmeraldGreenPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = EmeraldGreenPrimary
            )
        }
    }
}

@Composable
private fun AdviceSubSectionHeader(title: String) {
    val annotated = parseMarkdownToAnnotatedString(
        text = title,
        baseColor = MaterialTheme.colorScheme.onSurface,
        boldColor = EmeraldGreenPrimary
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldGreenPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AdviceBulletItem(content: String) {
    val annotated = parseMarkdownToAnnotatedString(
        text = content,
        baseColor = MaterialTheme.colorScheme.onSurface
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(EmeraldGreenPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp,
                fontSize = 13.5.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun isAdviceSectionHeader(line: String): Boolean {
    return line.matches(Regex("""^(?:\d+\.\s*\*\*|\*\*\d+\.|【).+"""))
}

private fun isNumberedListItem(line: String): Boolean {
    return line.matches(Regex("""^\d+\.\s+[^\*].*"""))
}

private fun parseMarkdownToAnnotatedString(
    text: String,
    baseColor: Color,
    boldColor: Color = baseColor
): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("""\*\*(.*?)\*\*""")
        val matches = regex.findAll(text)

        for (match in matches) {
            val range = match.range
            if (range.first > cursor) {
                withStyle(SpanStyle(color = baseColor)) {
                    append(text.substring(cursor, range.first))
                }
            }
            val boldContent = match.groupValues[1]
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)) {
                append(boldContent)
            }
            cursor = range.last + 1
        }

        if (cursor < text.length) {
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(cursor))
            }
        }
    }
}

