package com.example.foodlogger.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import com.example.foodlogger.data.model.NutritionAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class HealthConnectManager(private val context: Context) {

    val healthConnectClient by lazy {
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getWritePermission(NutritionRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun insertNutritionRecord(
        analysisResult: NutritionAnalysisResult,
        recordedTime: Instant = Instant.now()
    ): Result<String> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext Result.failure(
            IllegalStateException("Health Connect がこの端末で利用できません。")
        )

        try {
            val zoneOffset = ZonedDateTime.ofInstant(recordedTime, ZoneId.systemDefault()).offset
            val nutrients = analysisResult.nutrients

            val mealTypeConstant = when (analysisResult.mealType.uppercase()) {
                "BREAKFAST" -> MealType.MEAL_TYPE_BREAKFAST
                "LUNCH" -> MealType.MEAL_TYPE_LUNCH
                "DINNER" -> MealType.MEAL_TYPE_DINNER
                "SNACK" -> MealType.MEAL_TYPE_SNACK
                else -> MealType.MEAL_TYPE_UNKNOWN
            }

            // Sodium calculation: if sodiumMg is 0 but saltEquivalentG is present, calculate Na
            val effectiveSodiumMg = if (nutrients.sodiumMg > 0) {
                nutrients.sodiumMg
            } else if (nutrients.saltEquivalentG > 0) {
                (nutrients.saltEquivalentG * 1000.0) / 2.54
            } else {
                0.0
            }

            val record = NutritionRecord(
                startTime = recordedTime,
                startZoneOffset = zoneOffset,
                endTime = recordedTime.plusSeconds(60),
                endZoneOffset = zoneOffset,
                name = analysisResult.mealName.ifBlank { "食事記録" },
                mealType = mealTypeConstant,
                energy = if (nutrients.caloriesKcal > 0) Energy.kilocalories(nutrients.caloriesKcal) else null,
                protein = if (nutrients.proteinG > 0) Mass.grams(nutrients.proteinG) else null,
                totalFat = if (nutrients.fatG > 0) Mass.grams(nutrients.fatG) else null,
                totalCarbohydrate = if (nutrients.carbohydrateG > 0) Mass.grams(nutrients.carbohydrateG) else null,
                dietaryFiber = if (nutrients.fiberG > 0) Mass.grams(nutrients.fiberG) else null,
                sugar = if (nutrients.sugarG > 0) Mass.grams(nutrients.sugarG) else null,
                sodium = if (effectiveSodiumMg > 0) Mass.milligrams(effectiveSodiumMg) else null,
                potassium = if (nutrients.potassiumMg > 0) Mass.milligrams(nutrients.potassiumMg) else null,
                calcium = if (nutrients.calciumMg > 0) Mass.milligrams(nutrients.calciumMg) else null,
                iron = if (nutrients.ironMg > 0) Mass.milligrams(nutrients.ironMg) else null,
                zinc = if (nutrients.zincMg > 0) Mass.milligrams(nutrients.zincMg) else null,
                magnesium = if (nutrients.magnesiumMg > 0) Mass.milligrams(nutrients.magnesiumMg) else null,
                vitaminA = if (nutrients.vitaminAMcg > 0) Mass.micrograms(nutrients.vitaminAMcg) else null,
                thiamin = if (nutrients.vitaminB1Mg > 0) Mass.milligrams(nutrients.vitaminB1Mg) else null,
                riboflavin = if (nutrients.vitaminB2Mg > 0) Mass.milligrams(nutrients.vitaminB2Mg) else null,
                vitaminB6 = if (nutrients.vitaminB6Mg > 0) Mass.milligrams(nutrients.vitaminB6Mg) else null,
                vitaminB12 = if (nutrients.vitaminB12Mcg > 0) Mass.micrograms(nutrients.vitaminB12Mcg) else null,
                vitaminC = if (nutrients.vitaminCMg > 0) Mass.milligrams(nutrients.vitaminCMg) else null,
                vitaminD = if (nutrients.vitaminDMcg > 0) Mass.micrograms(nutrients.vitaminDMcg) else null,
                vitaminE = if (nutrients.vitaminEMg > 0) Mass.milligrams(nutrients.vitaminEMg) else null,
                folate = if (nutrients.folateMcg > 0) Mass.micrograms(nutrients.folateMcg) else null,
                saturatedFat = if (nutrients.saturatedFatG > 0) Mass.grams(nutrients.saturatedFatG) else null,
                transFat = if (nutrients.transFatG > 0) Mass.grams(nutrients.transFatG) else null,
                cholesterol = if (nutrients.cholesterolMg > 0) Mass.milligrams(nutrients.cholesterolMg) else null
            )

            val response = client.insertRecords(listOf(record))
            val recordId = response.recordIdsList.firstOrNull() ?: "unknown_id"
            Result.success(recordId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
