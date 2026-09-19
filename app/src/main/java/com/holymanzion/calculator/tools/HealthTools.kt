package com.holymanzion.calculator.tools

/**
 * Body mass index, using the WHO adult categories.
 *
 * BMI is a population-level screening figure, not a diagnosis — it ignores body
 * composition, so it reads high for muscular builds. The UI says so next to the result.
 */
object BmiCalculator {

    enum class Category(val label: String, val range: String) {
        Underweight("Underweight", "below 18.5"),
        Normal("Healthy weight", "18.5 – 24.9"),
        Overweight("Overweight", "25.0 – 29.9"),
        Obese("Obese", "30.0 and above"),
    }

    data class Result(
        val bmi: Double,
        val category: Category,
        val healthyMinKg: Double,
        val healthyMaxKg: Double,
    )

    private const val HEALTHY_LOW = 18.5
    private const val HEALTHY_HIGH = 24.9

    /** @return null when either input is non-positive, which has no meaningful BMI. */
    fun calculate(weightKg: Double, heightCm: Double): Result? {
        if (weightKg <= 0 || heightCm <= 0) return null

        val heightM = heightCm / 100
        val bmi = weightKg / (heightM * heightM)

        return Result(
            bmi = bmi,
            category = categoryFor(bmi),
            healthyMinKg = HEALTHY_LOW * heightM * heightM,
            healthyMaxKg = HEALTHY_HIGH * heightM * heightM,
        )
    }

    fun categoryFor(bmi: Double): Category = when {
        bmi < 18.5 -> Category.Underweight
        bmi < 25.0 -> Category.Normal
        bmi < 30.0 -> Category.Overweight
        else -> Category.Obese
    }

    /** Converts feet and inches to centimetres for the imperial input mode. */
    fun feetInchesToCm(feet: Double, inches: Double): Double = (feet * 12 + inches) * 2.54

    /** Converts pounds to kilograms for the imperial input mode. */
    fun poundsToKg(pounds: Double): Double = pounds * 0.45359237
}
