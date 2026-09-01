package com.example.foodlogger.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NutritionAnalysisResult(
    @SerialName("meal_name")
    val mealName: String = "",
    @SerialName("meal_type")
    val mealType: String = "LUNCH", // BREAKFAST, LUNCH, DINNER, SNACK
    @SerialName("dishes")
    val dishes: List<DishItem> = emptyList(),
    @SerialName("nutrients")
    val nutrients: NutrientDetails = NutrientDetails(),
    @SerialName("notes")
    val notes: String = ""
)

@Serializable
data class DishItem(
    @SerialName("name")
    val name: String,
    @SerialName("estimated_portion")
    val estimatedPortion: String = "",
    @SerialName("calories_kcal")
    val caloriesKcal: Double = 0.0
)

@Serializable
data class NutrientDetails(
    // Macro nutrients
    @SerialName("calories_kcal")
    val caloriesKcal: Double = 0.0,
    @SerialName("protein_g")
    val proteinG: Double = 0.0,
    @SerialName("fat_g")
    val fatG: Double = 0.0,
    @SerialName("carbohydrate_g")
    val carbohydrateG: Double = 0.0,
    @SerialName("fiber_g")
    val fiberG: Double = 0.0,
    @SerialName("sugar_g")
    val sugarG: Double = 0.0,

    // Minerals
    @SerialName("sodium_mg")
    val sodiumMg: Double = 0.0,
    @SerialName("salt_equivalent_g")
    val saltEquivalentG: Double = 0.0,
    @SerialName("potassium_mg")
    val potassiumMg: Double = 0.0,
    @SerialName("calcium_mg")
    val calciumMg: Double = 0.0,
    @SerialName("iron_mg")
    val ironMg: Double = 0.0,
    @SerialName("zinc_mg")
    val zincMg: Double = 0.0,
    @SerialName("magnesium_mg")
    val magnesiumMg: Double = 0.0,

    // Vitamins
    @SerialName("vitamin_a_mcg")
    val vitaminAMcg: Double = 0.0,
    @SerialName("vitamin_b1_mg")
    val vitaminB1Mg: Double = 0.0,
    @SerialName("vitamin_b2_mg")
    val vitaminB2Mg: Double = 0.0,
    @SerialName("vitamin_b6_mg")
    val vitaminB6Mg: Double = 0.0,
    @SerialName("vitamin_b12_mcg")
    val vitaminB12Mcg: Double = 0.0,
    @SerialName("vitamin_c_mg")
    val vitaminCMg: Double = 0.0,
    @SerialName("vitamin_d_mcg")
    val vitaminDMcg: Double = 0.0,
    @SerialName("vitamin_e_mg")
    val vitaminEMg: Double = 0.0,
    @SerialName("folate_mcg")
    val folateMcg: Double = 0.0,

    // Additional Fats & Cholesterol
    @SerialName("saturated_fat_g")
    val saturatedFatG: Double = 0.0,
    @SerialName("trans_fat_g")
    val transFatG: Double = 0.0,
    @SerialName("cholesterol_mg")
    val cholesterolMg: Double = 0.0
)
