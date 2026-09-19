package com.holymanzion.calculator.tools

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolsTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 1e-9) {
        assertEquals(expected, actual, tolerance)
    }

    // ---- unit conversion -------------------------------------------------

    @Test
    fun `length conversions round trip`() {
        val units = UnitCatalog.unitsFor(UnitCategory.Length)
        val metre = units.first { it.symbol == "m" }
        val foot = units.first { it.symbol == "ft" }

        assertClose(3.280839895013123, convertUnits(1.0, metre, foot))
        assertClose(1.0, convertUnits(convertUnits(1.0, metre, foot), foot, metre))
    }

    @Test
    fun `a mile is 1609_344 metres`() {
        val units = UnitCatalog.unitsFor(UnitCategory.Length)
        val mile = units.first { it.symbol == "mi" }
        val metre = units.first { it.symbol == "m" }
        assertClose(1609.344, convertUnits(1.0, mile, metre))
    }

    @Test
    fun `temperature uses the affine offset`() {
        val units = UnitCatalog.unitsFor(UnitCategory.Temperature)
        val celsius = units.first { it.symbol == "°C" }
        val fahrenheit = units.first { it.symbol == "°F" }
        val kelvin = units.first { it.symbol == "K" }

        assertClose(32.0, convertUnits(0.0, celsius, fahrenheit), 1e-9)
        assertClose(212.0, convertUnits(100.0, celsius, fahrenheit), 1e-9)
        assertClose(-40.0, convertUnits(-40.0, celsius, fahrenheit), 1e-9)
        assertClose(273.15, convertUnits(0.0, celsius, kelvin), 1e-9)
        assertClose(37.0, convertUnits(98.6, fahrenheit, celsius), 1e-9)
    }

    @Test
    fun `decimal and binary data units are distinct`() {
        val units = UnitCatalog.unitsFor(UnitCategory.Data)
        val megabyte = units.first { it.symbol == "MB" }
        val mebibyte = units.first { it.symbol == "MiB" }
        val byte = units.first { it.symbol == "B" }

        assertClose(1_000_000.0, convertUnits(1.0, megabyte, byte))
        assertClose(1_048_576.0, convertUnits(1.0, mebibyte, byte))
    }

    @Test
    fun `every category exposes at least two units and valid defaults`() {
        UnitCategory.entries.forEach { category ->
            val units = UnitCatalog.unitsFor(category)
            assertTrue("${category.label} needs at least two units", units.size >= 2)
            assertTrue(UnitCatalog.defaultFromIndex(category) in units.indices)
            assertTrue(UnitCatalog.defaultToIndex(category) in units.indices)
        }
    }

    // ---- discount --------------------------------------------------------

    @Test
    fun `single discount`() {
        val result = DiscountCalculator.calculate(originalPrice = 200.0, discountPercent = 25.0)
        assertClose(150.0, result.discountedPrice)
        assertClose(50.0, result.youSave)
        assertClose(150.0, result.finalPrice)
        assertClose(25.0, result.effectiveDiscountPercent)
    }

    @Test
    fun `stacked discounts multiply rather than add`() {
        val result = DiscountCalculator.calculate(
            originalPrice = 100.0,
            discountPercent = 20.0,
            extraDiscountPercent = 10.0,
        )
        // 20% then 10% is 28% off, not 30%.
        assertClose(72.0, result.discountedPrice)
        assertClose(28.0, result.effectiveDiscountPercent)
    }

    @Test
    fun `tax applies after the discount`() {
        val result = DiscountCalculator.calculate(
            originalPrice = 100.0,
            discountPercent = 50.0,
            taxPercent = 10.0,
        )
        assertClose(50.0, result.discountedPrice)
        assertClose(5.0, result.taxAmount)
        assertClose(55.0, result.finalPrice)
    }

    // ---- price and bill --------------------------------------------------

    @Test
    fun `tip defaults to the pre-tax amount`() {
        val result = PriceCalculator.calculate(
            amount = 100.0,
            taxPercent = 10.0,
            tipPercent = 20.0,
        )
        assertClose(10.0, result.taxAmount)
        assertClose(20.0, result.tipAmount)
        assertClose(130.0, result.total)
    }

    @Test
    fun `tipping after tax also tips the tax`() {
        val result = PriceCalculator.calculate(
            amount = 100.0,
            taxPercent = 10.0,
            tipPercent = 20.0,
            tipOnPreTaxAmount = false,
        )
        assertClose(22.0, result.tipAmount)
        assertClose(132.0, result.total)
    }

    @Test
    fun `splitting divides the total`() {
        val result = PriceCalculator.calculate(amount = 90.0, people = 3)
        assertClose(30.0, result.perPerson)
    }

    @Test
    fun `a zero head count never divides by zero`() {
        val result = PriceCalculator.calculate(amount = 50.0, people = 0)
        assertClose(50.0, result.perPerson)
    }

    // ---- loan ------------------------------------------------------------

    @Test
    fun `standard amortised repayment`() {
        // 100,000 over 10 years at 6% is a well-known ~1110.21 per month.
        val result = LoanCalculator.calculate(100_000.0, 6.0, 120)
        assertEquals(1110.21, result.monthlyPayment, 0.01)
        assertEquals(133_224.6, result.totalPayable, 1.0)
        assertTrue(result.totalInterest > 0)
    }

    @Test
    fun `a zero rate loan is the principal split evenly`() {
        val result = LoanCalculator.calculate(1200.0, 0.0, 12)
        assertClose(100.0, result.monthlyPayment)
        assertClose(0.0, result.totalInterest)
        assertClose(1200.0, result.totalPayable)
    }

    // ---- percentage ------------------------------------------------------

    @Test
    fun `percentage modes`() {
        assertClose(25.0, PercentageCalculator.percentOf(25.0, 100.0))
        assertClose(12.5, PercentageCalculator.percentOf(50.0, 25.0))
        assertClose(50.0, PercentageCalculator.whatPercent(25.0, 50.0)!!)
        assertClose(50.0, PercentageCalculator.change(100.0, 150.0)!!)
        assertClose(-50.0, PercentageCalculator.change(100.0, 50.0)!!)
    }

    @Test
    fun `percentage against zero is undefined rather than infinite`() {
        assertNull(PercentageCalculator.whatPercent(5.0, 0.0))
        assertNull(PercentageCalculator.change(0.0, 10.0))
    }

    // ---- dates -----------------------------------------------------------

    @Test
    fun `days between dates`() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 12, 31)
        val difference = DateCalculator.between(start, end)

        assertEquals(364L, difference.totalDays)
        assertEquals(52L, difference.weeks)
        assertEquals(0, difference.years)
        assertEquals(11, difference.months)
    }

    @Test
    fun `difference is symmetric`() {
        val a = LocalDate.of(2026, 3, 1)
        val b = LocalDate.of(2026, 4, 1)
        assertEquals(
            DateCalculator.between(a, b).totalDays,
            DateCalculator.between(b, a).totalDays,
        )
    }

    @Test
    fun `leap years are counted`() {
        val difference = DateCalculator.between(
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 3, 1),
        )
        assertEquals(29L, difference.totalDays)
    }

    @Test
    fun `adding a month clamps to a short month end`() {
        val result = DateCalculator.shift(LocalDate.of(2026, 1, 31), months = 1)
        assertEquals(LocalDate.of(2026, 2, 28), result)
    }

    @Test
    fun `shifting backwards uses negative amounts`() {
        val result = DateCalculator.shift(LocalDate.of(2026, 3, 15), days = -14)
        assertEquals(LocalDate.of(2026, 3, 1), result)
    }

    @Test
    fun `shifting by weeks and days combines`() {
        val result = DateCalculator.shift(LocalDate.of(2026, 1, 1), weeks = 2, days = 3)
        assertEquals(LocalDate.of(2026, 1, 18), result)
    }

    // ---- bmi -------------------------------------------------------------

    @Test
    fun `bmi and category`() {
        val result = BmiCalculator.calculate(weightKg = 70.0, heightCm = 175.0)!!
        assertEquals(22.86, result.bmi, 0.01)
        assertEquals(BmiCalculator.Category.Normal, result.category)
    }

    @Test
    fun `category boundaries`() {
        assertEquals(BmiCalculator.Category.Underweight, BmiCalculator.categoryFor(18.4))
        assertEquals(BmiCalculator.Category.Normal, BmiCalculator.categoryFor(18.5))
        assertEquals(BmiCalculator.Category.Normal, BmiCalculator.categoryFor(24.9))
        assertEquals(BmiCalculator.Category.Overweight, BmiCalculator.categoryFor(25.0))
        assertEquals(BmiCalculator.Category.Obese, BmiCalculator.categoryFor(30.0))
    }

    @Test
    fun `non-positive inputs have no bmi`() {
        assertNull(BmiCalculator.calculate(0.0, 175.0))
        assertNull(BmiCalculator.calculate(70.0, 0.0))
        assertNull(BmiCalculator.calculate(-5.0, 175.0))
    }

    @Test
    fun `imperial conversions`() {
        assertClose(180.34, BmiCalculator.feetInchesToCm(5.0, 11.0), 1e-9)
        assertClose(68.0388555, BmiCalculator.poundsToKg(150.0), 1e-6)
    }

    @Test
    fun `healthy weight range brackets the normal category`() {
        val result = BmiCalculator.calculate(weightKg = 70.0, heightCm = 175.0)!!
        assertEquals(
            BmiCalculator.Category.Normal,
            BmiCalculator.categoryFor(
                BmiCalculator.calculate(result.healthyMinKg, 175.0)!!.bmi,
            ),
        )
        assertEquals(
            BmiCalculator.Category.Normal,
            BmiCalculator.categoryFor(
                BmiCalculator.calculate(result.healthyMaxKg, 175.0)!!.bmi,
            ),
        )
    }
}
