package com.example.foodlogger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class MealCategory(val key: String, val label: String, val mealTypeConstant: Int) {
    BREAKFAST("BREAKFAST", "朝食", MealType.MEAL_TYPE_BREAKFAST),
    LUNCH("LUNCH", "昼食", MealType.MEAL_TYPE_LUNCH),
    DINNER("DINNER", "夕食", MealType.MEAL_TYPE_DINNER),
    SNACK("SNACK", "間食", MealType.MEAL_TYPE_SNACK),
    OTHER("OTHER", "その他", MealType.MEAL_TYPE_UNKNOWN)
}

sealed interface MealRecordStatus {
    object NotRegistered : MealRecordStatus
    object Skipped : MealRecordStatus
    data class Registered(val record: NutritionRecord, val caloriesKcal: Double) : MealRecordStatus
}

@Composable
fun WeeklyDashboardScreen(
    currentWeekStart: LocalDate,
    weekRecordsMap: Map<LocalDate, Map<Int, NutritionRecord>>,
    previousWeekUnregisteredList: List<Pair<LocalDate, MealCategory>>,
    ageGroup: String,
    gender: String,
    activityLevel: String,
    onPreviousWeekClick: () -> Unit,
    onNextWeekClick: () -> Unit,
    onCurrentWeekClick: () -> Unit,
    onCellClick: (LocalDate, MealCategory) -> Unit,
    onDailySummaryClick: (LocalDate) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val earliestAllowedDate = today.minusDays(30) // Health Connect 30-day limit
    val currentWeekOfToday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val isViewingCurrentWeek = currentWeekStart == currentWeekOfToday

    val canGoPreviousWeek = currentWeekStart.minusWeeks(1).plusDays(6) >= earliestAllowedDate
    val canGoNextWeek = currentWeekStart < currentWeekOfToday

    val weekDays = (0..6).map { currentWeekStart.plusDays(it.toLong()) }
    val formatterHeader = DateTimeFormatter.ofPattern("M/d(E)", Locale.JAPANESE)
    val weekRangeText = "${currentWeekStart.format(DateTimeFormatter.ofPattern("M/d(E)", Locale.JAPANESE))} 〜 ${weekDays.last().format(DateTimeFormatter.ofPattern("M/d(E)", Locale.JAPANESE))}"
    val targetStandards = com.example.foodlogger.data.model.NutritionStandards.getDailyTarget(ageGroup, gender, activityLevel)

    // Scroll state for horizontal matrix
    val matrixScrollState = rememberScrollState()

    // Auto-scroll to end on Thursday, Friday, Saturday if viewing current week
    val isLaterHalfOfWeek = today.dayOfWeek in listOf(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    LaunchedEffect(currentWeekStart) {
        if (isViewingCurrentWeek && isLaterHalfOfWeek) {
            matrixScrollState.scrollTo(matrixScrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Navigation Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPreviousWeekClick,
                    enabled = canGoPreviousWeek
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "前週へ",
                        tint = if (canGoPreviousWeek) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${currentWeekStart.year}年",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = weekRangeText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNextWeekClick,
                        enabled = canGoNextWeek
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "来週へ",
                            tint = if (canGoNextWeek) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    if (!isViewingCurrentWeek) {
                        FilledTonalButton(
                            onClick = onCurrentWeekClick,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("今週", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Weekly Matrix Card (Fixed Left Labels + Horizontally Scrollable Days)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 1. Fixed Left Column (Category Labels)
                Column(
                    modifier = Modifier.width(56.dp)
                ) {
                    // Empty Top-Left Cell for Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Empty corner
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Labels
                    MealCategory.values().forEach { category ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .height(58.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Summary Label
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(58.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreenPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "総括",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreenPrimary
                            )
                            Text(
                                text = "日次評価",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = EmeraldGreenPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 2. Horizontally Scrollable 7 Days Data Grid
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(matrixScrollState)
                ) {
                    Column {
                        // Header Row: 7 Days
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            weekDays.forEach { date ->
                                val isToday = date == today
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .height(38.dp)
                                        .padding(horizontal = 2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isToday) EmeraldGreenPrimary.copy(alpha = 0.15f) else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = date.format(formatterHeader),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) EmeraldGreenPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Rows
                        MealCategory.values().forEach { category ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                weekDays.forEach { date ->
                                    val dayRecords = weekRecordsMap[date] ?: emptyMap()
                                    val record = dayRecords[category.mealTypeConstant]
                                    val isOutOfRange = date < earliestAllowedDate

                                    val status: MealRecordStatus = when {
                                        record == null -> MealRecordStatus.NotRegistered
                                        record.name == "食事なし" || (record.energy?.inKilocalories ?: 0.0) == 0.0 -> MealRecordStatus.Skipped
                                        else -> MealRecordStatus.Registered(record, record.energy?.inKilocalories ?: 0.0)
                                    }

                                    MatrixCell(
                                        status = status,
                                        isToday = date == today,
                                        isOutOfRange = isOutOfRange,
                                        onClick = { if (!isOutOfRange) onCellClick(date, category) },
                                        modifier = Modifier
                                            .width(74.dp)
                                            .height(58.dp)
                                            .padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }

                        // Summary Row (総括)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            weekDays.forEach { date ->
                                val dayRecords = weekRecordsMap[date] ?: emptyMap()
                                val totalCalories = dayRecords.values.sumOf { it.energy?.inKilocalories ?: 0.0 }
                                val hasBreakfast = dayRecords.containsKey(MealCategory.BREAKFAST.mealTypeConstant)
                                val hasLunch = dayRecords.containsKey(MealCategory.LUNCH.mealTypeConstant)
                                val hasDinner = dayRecords.containsKey(MealCategory.DINNER.mealTypeConstant)
                                val hasCompletedMain = hasBreakfast && hasLunch && hasDinner

                                val totalP = dayRecords.values.sumOf { it.protein?.inGrams ?: 0.0 }
                                val totalF = dayRecords.values.sumOf { it.totalFat?.inGrams ?: 0.0 }
                                val totalC = dayRecords.values.sumOf { it.totalCarbohydrate?.inGrams ?: 0.0 }
                                val totalMacroKcal = (totalP * 4.0) + (totalF * 9.0) + (totalC * 4.0)
                                val pPct = if (totalMacroKcal > 0) ((totalP * 4.0) / totalMacroKcal * 100).toInt() else 0
                                val fPct = if (totalMacroKcal > 0) ((totalF * 9.0) / totalMacroKcal * 100).toInt() else 0
                                val cPct = if (totalMacroKcal > 0) ((totalC * 4.0) / totalMacroKcal * 100).toInt() else 0

                                val eval = com.example.foodlogger.data.model.NutritionStandards.evaluateDailyIntake(
                                    hasCompletedMainMeals = hasCompletedMain,
                                    totalCalories = totalCalories,
                                    targetCalories = targetStandards.targetCaloriesKcal,
                                    pPct = pPct,
                                    fPct = fPct,
                                    cPct = cPct
                                )

                                SummaryCell(
                                    date = date,
                                    totalCalories = totalCalories,
                                    evaluation = eval,
                                    hasAnyRecord = dayRecords.isNotEmpty(),
                                    onClick = { onDailySummaryClick(date) },
                                    modifier = Modifier
                                        .width(74.dp)
                                        .height(58.dp)
                                        .padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Previous Week Unregistered Alert (Shown only when viewing Current Week)
        if (isViewingCurrentWeek) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (previousWeekUnregisteredList.isEmpty()) {
                        EmeraldGreenPrimary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (previousWeekUnregisteredList.isEmpty()) Icons.Default.EventAvailable else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (previousWeekUnregisteredList.isEmpty()) EmeraldGreenPrimary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "前週（過去7日間）の未登録チェック",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (previousWeekUnregisteredList.isEmpty()) {
                        Text(
                            text = "前週の未登録: なし 🎉（朝・昼・夕すべて記録済みです）",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = EmeraldGreenPrimary
                        )
                    } else {
                        Text(
                            text = "前週（朝・昼・夕）に以下の未登録があります。タップして記録できます：",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            previousWeekUnregisteredList.forEach { (date, cat) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = ButtonDefaults.outlinedButtonBorder,
                                    modifier = Modifier.clickable { onCellClick(date, cat) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${date.format(formatterHeader)} ${cat.label}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixCell(
    status: MealRecordStatus,
    isToday: Boolean,
    isOutOfRange: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(10.dp)

    when (status) {
        is MealRecordStatus.NotRegistered -> {
            Box(
                modifier = modifier
                    .clip(cellShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isOutOfRange) 0.2f else 0.7f), cellShape)
                    .background(if (isOutOfRange) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else if (isToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface)
                    .clickable(enabled = !isOutOfRange) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "未登録",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isOutOfRange) 0.15f else 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOutOfRange) "ー" else "未登録",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isOutOfRange) 0.3f else 0.6f)
                    )
                }
            }
        }
        is MealRecordStatus.Skipped -> {
            Box(
                modifier = modifier
                    .clip(cellShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoNotDisturbOn,
                        contentDescription = "食事なし",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "食事なし",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        is MealRecordStatus.Registered -> {
            Box(
                modifier = modifier
                    .clip(cellShape)
                    .background(EmeraldGreenPrimary.copy(alpha = 0.15f))
                    .border(1.dp, EmeraldGreenPrimary.copy(alpha = 0.4f), cellShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "登録済",
                            tint = Color.White,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${status.caloriesKcal.toInt()}kcal",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = EmeraldGreenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCell(
    date: LocalDate,
    totalCalories: Double,
    evaluation: com.example.foodlogger.data.model.EvaluationGrade,
    hasAnyRecord: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .clip(cellShape)
            .background(if (hasAnyRecord) evaluation.badgeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, if (hasAnyRecord) evaluation.badgeColor.copy(alpha = 0.4f) else Color.Transparent, cellShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasAnyRecord) {
                Text(
                    text = "${totalCalories.toInt()}k",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = evaluation.badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = evaluation.label.take(4),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                        color = evaluation.badgeColor,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            } else {
                Text(
                    text = "ー",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
