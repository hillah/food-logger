package com.example.foodlogger.data.model

import androidx.compose.ui.graphics.Color
import com.example.foodlogger.ui.theme.EmeraldGreenPrimary

data class DailyNutritionTarget(
    val ageGroupLabel: String,
    val genderLabel: String,
    val activityLevelLabel: String,
    val targetCaloriesKcal: Double,
    val targetProteinG: Double,
    val targetFatG: Double,
    val targetCarbsG: Double,
    val targetFiberG: Double,
    val maxSaltG: Double,
    val targetCalciumMg: Double,
    val targetIronMg: Double,
    val targetZincMg: Double,
    val targetMagnesiumMg: Double,
    val targetVitaminAMcg: Double,
    val targetVitaminB1Mg: Double,
    val targetVitaminB2Mg: Double,
    val targetVitaminCMg: Double,
    val targetVitaminDMcg: Double,
    val targetVitaminEMg: Double,
    val targetFolateMcg: Double
)

enum class EvaluationGrade(val label: String, val badgeColor: Color, val description: String) {
    EXCELLENT("良好 ◎", EmeraldGreenPrimary, "カロリー・栄養素ともに理想的なバランスです"),
    GOOD("適正 ○", EmeraldGreenPrimary, "全体的に概ね適正な範囲に収まっています"),
    CALORIE_OVER("カロリー多め ⚠️", Color(0xFFE65100), "1日の目安カロリーを上回っています"),
    CALORIE_UNDER("カロリー控えめ ⚠️", Color(0xFF1976D2), "1日の目安カロリーに達していません"),
    INCOMPLETE("未完了 📝", Color(0xFF757575), "朝食・昼食・夕食の記録が完了していません")
}

object NutritionStandards {

    fun getDailyTarget(ageGroup: String, gender: String, activityLevel: String = "low"): DailyNutritionTarget {
        val isFemale = gender.equals("female", ignoreCase = true)
        val genderLabel = if (isFemale) "女性" else if (gender.equals("other", ignoreCase = true)) "一般" else "男性"

        val ageLabel = when (ageGroup) {
            "10s" -> "10代"
            "20s" -> "20代"
            "30s" -> "30代"
            "40s" -> "40代"
            "50s" -> "50代"
            "60s" -> "60代"
            "70s_plus" -> "70代以上"
            else -> "40代"
        }

        val activityLabel = when (activityLevel) {
            "high" -> "活動量:高"
            "normal" -> "活動量:普"
            else -> "活動量:低"
        }

        // Japanese Dietary Reference Intakes by Physical Activity Level (I: low, II: normal, III: high)
        val calories = if (isFemale) {
            when (activityLevel) {
                "high" -> when (ageGroup) {
                    "10s" -> 2600.0
                    "20s" -> 2300.0
                    "30s" -> 2350.0
                    "40s" -> 2350.0
                    "50s" -> 2250.0
                    "60s" -> 2100.0
                    "70s_plus" -> 1950.0
                    else -> 2350.0
                }
                "normal" -> when (ageGroup) {
                    "10s" -> 2300.0
                    "20s" -> 2000.0
                    "30s" -> 2050.0
                    "40s" -> 2050.0
                    "50s" -> 1950.0
                    "60s" -> 1850.0
                    "70s_plus" -> 1700.0
                    else -> 2050.0
                }
                else -> when (ageGroup) { // "low" (Level I)
                    "10s" -> 2000.0
                    "20s" -> 1700.0
                    "30s" -> 1750.0
                    "40s" -> 1750.0
                    "50s" -> 1650.0
                    "60s" -> 1550.0
                    "70s_plus" -> 1450.0
                    else -> 1750.0
                }
            }
        } else {
            when (activityLevel) {
                "high" -> when (ageGroup) {
                    "10s" -> 3100.0
                    "20s" -> 3050.0
                    "30s" -> 3050.0
                    "40s" -> 3050.0
                    "50s" -> 2950.0
                    "60s" -> 2750.0
                    "70s_plus" -> 2450.0
                    else -> 3050.0
                }
                "normal" -> when (ageGroup) {
                    "10s" -> 2750.0
                    "20s" -> 2700.0
                    "30s" -> 2700.0
                    "40s" -> 2700.0
                    "50s" -> 2600.0
                    "60s" -> 2400.0
                    "70s_plus" -> 2150.0
                    else -> 2700.0
                }
                else -> when (ageGroup) { // "low" (Level I)
                    "10s" -> 2400.0
                    "20s" -> 2300.0
                    "30s" -> 2300.0
                    "40s" -> 2300.0
                    "50s" -> 2200.0
                    "60s" -> 2050.0
                    "70s_plus" -> 1850.0
                    else -> 2300.0
                }
            }
        }

        val proteinG = if (isFemale) 50.0 else 65.0
        val fatG = (calories * 0.25) / 9.0 // 25% kcal from fat
        val carbsG = (calories * 0.575) / 4.0 // 57.5% kcal from carbs
        val fiberG = if (isFemale) 18.0 else 21.0
        val maxSaltG = if (isFemale) 6.5 else 7.5

        return DailyNutritionTarget(
            ageGroupLabel = ageLabel,
            genderLabel = genderLabel,
            activityLevelLabel = activityLabel,
            targetCaloriesKcal = calories,
            targetProteinG = proteinG,
            targetFatG = fatG,
            targetCarbsG = carbsG,
            targetFiberG = fiberG,
            maxSaltG = maxSaltG,
            targetCalciumMg = if (isFemale) 650.0 else 750.0,
            targetIronMg = if (isFemale) 10.5 else 7.5,
            targetZincMg = if (isFemale) 8.0 else 11.0,
            targetMagnesiumMg = if (isFemale) 290.0 else 370.0,
            targetVitaminAMcg = if (isFemale) 700.0 else 850.0,
            targetVitaminB1Mg = if (isFemale) 1.1 else 1.4,
            targetVitaminB2Mg = if (isFemale) 1.2 else 1.6,
            targetVitaminCMg = 100.0,
            targetVitaminDMcg = 8.5,
            targetVitaminEMg = if (isFemale) 6.0 else 6.5,
            targetFolateMcg = 240.0
        )
    }

    fun evaluateDailyIntake(
        hasCompletedMainMeals: Boolean,
        totalCalories: Double,
        targetCalories: Double,
        pPct: Int,
        fPct: Int,
        cPct: Int
    ): EvaluationGrade {
        if (!hasCompletedMainMeals) {
            return EvaluationGrade.INCOMPLETE
        }

        val calorieRatio = if (targetCalories > 0) totalCalories / targetCalories else 1.0

        return when {
            calorieRatio > 1.20 -> EvaluationGrade.CALORIE_OVER
            calorieRatio < 0.70 -> EvaluationGrade.CALORIE_UNDER
            pPct in 13..20 && fPct in 20..30 && cPct in 50..65 && calorieRatio in 0.85..1.15 -> EvaluationGrade.EXCELLENT
            else -> EvaluationGrade.GOOD
        }
    }
}
