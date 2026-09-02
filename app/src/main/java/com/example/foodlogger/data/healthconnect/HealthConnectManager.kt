package com.example.foodlogger.data.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import com.example.foodlogger.data.model.NutritionAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
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
        HealthPermission.getWritePermission(NutritionRecord::class),
        HealthPermission.getReadPermission(NutritionRecord::class)
    )

    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    /**
     * Read NutritionRecords within the specified time range.
     */
    suspend fun readNutritionRecords(
        startTime: Instant,
        endTime: Instant
    ): Result<List<NutritionRecord>> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext Result.failure(
            IllegalStateException("Health Connect が利用できません。")
        )
        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            Result.success(response.records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete records by ID.
     */
    suspend fun deleteNutritionRecords(recordIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext Result.failure(
            IllegalStateException("Health Connect が利用できません。")
        )
        try {
            client.deleteRecords(
                recordType = NutritionRecord::class,
                recordIdsList = recordIds,
                clientRecordIdsList = emptyList()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save/Overwrite nutrition record for a specific date and meal type.
     */
    suspend fun upsertNutritionRecord(
        analysisResult: NutritionAnalysisResult,
        targetDate: LocalDate,
        mealTypeString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext Result.failure(
            IllegalStateException("Health Connect が利用できません。")
        )

        try {
            val zoneId = ZoneId.systemDefault()
            val dayStart = targetDate.atStartOfDay(zoneId).toInstant()
            val dayEnd = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)

            val mealTypeConstant = parseMealType(mealTypeString)

            // 1. Check and delete existing records for the same day & mealType
            val existing = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(dayStart, dayEnd)
                )
            ).records

            val toDelete = existing.filter { it.mealType == mealTypeConstant }.map { it.metadata.id }
            if (toDelete.isNotEmpty()) {
                client.deleteRecords(
                    recordType = NutritionRecord::class,
                    recordIdsList = toDelete,
                    clientRecordIdsList = emptyList()
                )
            }

            // 2. Determine recorded time based on meal type and targetDate (ensure not in future)
            val recordedTime = getTargetTimeForMeal(targetDate, mealTypeConstant, zoneId)
            val now = Instant.now()
            val safeStartTime = if (recordedTime.isAfter(now)) now.minusSeconds(60) else recordedTime
            val safeEndTime = if (safeStartTime.plusSeconds(60).isAfter(now)) now else safeStartTime.plusSeconds(60)
            val zoneOffset = ZonedDateTime.ofInstant(safeStartTime, zoneId).offset
            val nutrients = analysisResult.nutrients

            val effectiveSodiumMg = if (nutrients.sodiumMg > 0) {
                nutrients.sodiumMg
            } else if (nutrients.saltEquivalentG > 0) {
                (nutrients.saltEquivalentG * 1000.0) / 2.54
            } else {
                0.0
            }

            val record = NutritionRecord(
                startTime = safeStartTime,
                startZoneOffset = zoneOffset,
                endTime = safeEndTime,
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

    /**
     * Record a skipped meal (0 kcal) for a specific date and meal type.
     */
    suspend fun insertSkippedMealRecord(
        targetDate: LocalDate,
        mealTypeString: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext Result.failure(
            IllegalStateException("Health Connect が利用できません。")
        )

        try {
            val zoneId = ZoneId.systemDefault()
            val dayStart = targetDate.atStartOfDay(zoneId).toInstant()
            val dayEnd = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)

            val mealTypeConstant = parseMealType(mealTypeString)

            // Delete existing records for the same day & mealType
            val existing = client.readRecords(
                ReadRecordsRequest(
                    recordType = NutritionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(dayStart, dayEnd)
                )
            ).records

            val toDelete = existing.filter { it.mealType == mealTypeConstant }.map { it.metadata.id }
            if (toDelete.isNotEmpty()) {
                client.deleteRecords(
                    recordType = NutritionRecord::class,
                    recordIdsList = toDelete,
                    clientRecordIdsList = emptyList()
                )
            }

            val recordedTime = getTargetTimeForMeal(targetDate, mealTypeConstant, zoneId)
            val now = Instant.now()
            val safeStartTime = if (recordedTime.isAfter(now)) now.minusSeconds(60) else recordedTime
            val safeEndTime = if (safeStartTime.plusSeconds(60).isAfter(now)) now else safeStartTime.plusSeconds(60)
            val zoneOffset = ZonedDateTime.ofInstant(safeStartTime, zoneId).offset

            val record = NutritionRecord(
                startTime = safeStartTime,
                startZoneOffset = zoneOffset,
                endTime = safeEndTime,
                endZoneOffset = zoneOffset,
                name = "食事なし",
                mealType = mealTypeConstant,
                energy = Energy.kilocalories(0.0),
                protein = Mass.grams(0.0),
                totalFat = Mass.grams(0.0),
                totalCarbohydrate = Mass.grams(0.0)
            )

            val response = client.insertRecords(listOf(record))
            val recordId = response.recordIdsList.firstOrNull() ?: "skipped_id"
            Result.success(recordId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseMealType(mealTypeString: String): Int {
        return when (mealTypeString.uppercase()) {
            "BREAKFAST" -> MealType.MEAL_TYPE_BREAKFAST
            "LUNCH" -> MealType.MEAL_TYPE_LUNCH
            "DINNER" -> MealType.MEAL_TYPE_DINNER
            "SNACK" -> MealType.MEAL_TYPE_SNACK
            else -> MealType.MEAL_TYPE_UNKNOWN
        }
    }

    private fun getTargetTimeForMeal(date: LocalDate, mealType: Int, zoneId: ZoneId): Instant {
        val time = when (mealType) {
            MealType.MEAL_TYPE_BREAKFAST -> date.atTime(8, 0)
            MealType.MEAL_TYPE_LUNCH -> date.atTime(12, 30)
            MealType.MEAL_TYPE_DINNER -> date.atTime(19, 0)
            MealType.MEAL_TYPE_SNACK -> date.atTime(15, 0)
            else -> date.atTime(21, 0)
        }
        return time.atZone(zoneId).toInstant()
    }
}
