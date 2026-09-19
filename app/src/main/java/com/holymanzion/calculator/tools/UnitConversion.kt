package com.holymanzion.calculator.tools

/**
 * Unit conversion.
 *
 * Every unit is defined by how it maps onto its category's base unit as
 * `base = value * factor + offset`. The affine [offset] term exists for temperature,
 * where the scales do not share a zero point; every other category leaves it at 0 and
 * reduces to a plain multiplication.
 */
data class MeasureUnit(
    val name: String,
    val symbol: String,
    val factor: Double,
    val offset: Double = 0.0,
)

enum class UnitCategory(val label: String) {
    Length("Length"),
    Mass("Weight & mass"),
    Temperature("Temperature"),
    Area("Area"),
    Volume("Volume"),
    Speed("Speed"),
    Time("Time"),
    Data("Digital storage"),
    Energy("Energy"),
    Pressure("Pressure"),
}

object UnitCatalog {

    /** Base unit: metre. */
    private val length = listOf(
        MeasureUnit("Millimetre", "mm", 0.001),
        MeasureUnit("Centimetre", "cm", 0.01),
        MeasureUnit("Metre", "m", 1.0),
        MeasureUnit("Kilometre", "km", 1000.0),
        MeasureUnit("Inch", "in", 0.0254),
        MeasureUnit("Foot", "ft", 0.3048),
        MeasureUnit("Yard", "yd", 0.9144),
        MeasureUnit("Mile", "mi", 1609.344),
        MeasureUnit("Nautical mile", "nmi", 1852.0),
    )

    /** Base unit: kilogram. */
    private val mass = listOf(
        MeasureUnit("Milligram", "mg", 1e-6),
        MeasureUnit("Gram", "g", 0.001),
        MeasureUnit("Kilogram", "kg", 1.0),
        MeasureUnit("Tonne", "t", 1000.0),
        MeasureUnit("Ounce", "oz", 0.028349523125),
        MeasureUnit("Pound", "lb", 0.45359237),
        MeasureUnit("Stone", "st", 6.35029318),
    )

    /**
     * Base unit: degrees Celsius.
     *
     * Fahrenheit is `C = F * 5/9 - 160/9`, which is where its offset comes from.
     */
    private val temperature = listOf(
        MeasureUnit("Celsius", "°C", 1.0),
        MeasureUnit("Fahrenheit", "°F", 5.0 / 9.0, -160.0 / 9.0),
        MeasureUnit("Kelvin", "K", 1.0, -273.15),
    )

    /** Base unit: square metre. */
    private val area = listOf(
        MeasureUnit("Square millimetre", "mm²", 1e-6),
        MeasureUnit("Square centimetre", "cm²", 1e-4),
        MeasureUnit("Square metre", "m²", 1.0),
        MeasureUnit("Hectare", "ha", 10_000.0),
        MeasureUnit("Square kilometre", "km²", 1e6),
        MeasureUnit("Square inch", "in²", 0.00064516),
        MeasureUnit("Square foot", "ft²", 0.09290304),
        MeasureUnit("Square yard", "yd²", 0.83612736),
        MeasureUnit("Acre", "ac", 4046.8564224),
        MeasureUnit("Square mile", "mi²", 2_589_988.110336),
    )

    /** Base unit: litre. */
    private val volume = listOf(
        MeasureUnit("Millilitre", "ml", 0.001),
        MeasureUnit("Litre", "l", 1.0),
        MeasureUnit("Cubic metre", "m³", 1000.0),
        MeasureUnit("Teaspoon (US)", "tsp", 0.00492892159375),
        MeasureUnit("Tablespoon (US)", "tbsp", 0.01478676478125),
        MeasureUnit("Fluid ounce (US)", "fl oz", 0.0295735295625),
        MeasureUnit("Cup (US)", "cup", 0.2365882365),
        MeasureUnit("Pint (US)", "pt", 0.473176473),
        MeasureUnit("Quart (US)", "qt", 0.946352946),
        MeasureUnit("Gallon (US)", "gal", 3.785411784),
        MeasureUnit("Gallon (UK)", "gal UK", 4.54609),
    )

    /** Base unit: metre per second. */
    private val speed = listOf(
        MeasureUnit("Metre per second", "m/s", 1.0),
        MeasureUnit("Kilometre per hour", "km/h", 1.0 / 3.6),
        MeasureUnit("Mile per hour", "mph", 0.44704),
        MeasureUnit("Foot per second", "ft/s", 0.3048),
        MeasureUnit("Knot", "kn", 1852.0 / 3600.0),
    )

    /** Base unit: second. */
    private val time = listOf(
        MeasureUnit("Millisecond", "ms", 0.001),
        MeasureUnit("Second", "s", 1.0),
        MeasureUnit("Minute", "min", 60.0),
        MeasureUnit("Hour", "h", 3600.0),
        MeasureUnit("Day", "d", 86_400.0),
        MeasureUnit("Week", "wk", 604_800.0),
        MeasureUnit("Month (30 days)", "mo", 2_592_000.0),
        MeasureUnit("Year (365 days)", "yr", 31_536_000.0),
    )

    /**
     * Base unit: byte.
     *
     * Both the decimal (kB = 1000) and binary (KiB = 1024) families are listed, because
     * conflating them is exactly the mistake this tool should help avoid.
     */
    private val data = listOf(
        MeasureUnit("Bit", "b", 0.125),
        MeasureUnit("Byte", "B", 1.0),
        MeasureUnit("Kilobyte", "kB", 1000.0),
        MeasureUnit("Kibibyte", "KiB", 1024.0),
        MeasureUnit("Megabyte", "MB", 1e6),
        MeasureUnit("Mebibyte", "MiB", 1048576.0),
        MeasureUnit("Gigabyte", "GB", 1e9),
        MeasureUnit("Gibibyte", "GiB", 1073741824.0),
        MeasureUnit("Terabyte", "TB", 1e12),
        MeasureUnit("Tebibyte", "TiB", 1099511627776.0),
    )

    /** Base unit: joule. */
    private val energy = listOf(
        MeasureUnit("Joule", "J", 1.0),
        MeasureUnit("Kilojoule", "kJ", 1000.0),
        MeasureUnit("Calorie", "cal", 4.184),
        MeasureUnit("Kilocalorie", "kcal", 4184.0),
        MeasureUnit("Watt hour", "Wh", 3600.0),
        MeasureUnit("Kilowatt hour", "kWh", 3.6e6),
        MeasureUnit("British thermal unit", "BTU", 1055.05585262),
    )

    /** Base unit: pascal. */
    private val pressure = listOf(
        MeasureUnit("Pascal", "Pa", 1.0),
        MeasureUnit("Kilopascal", "kPa", 1000.0),
        MeasureUnit("Bar", "bar", 100_000.0),
        MeasureUnit("Pound per square inch", "psi", 6894.757293168),
        MeasureUnit("Atmosphere", "atm", 101_325.0),
        MeasureUnit("Millimetre of mercury", "mmHg", 133.322387415),
    )

    fun unitsFor(category: UnitCategory): List<MeasureUnit> = when (category) {
        UnitCategory.Length -> length
        UnitCategory.Mass -> mass
        UnitCategory.Temperature -> temperature
        UnitCategory.Area -> area
        UnitCategory.Volume -> volume
        UnitCategory.Speed -> speed
        UnitCategory.Time -> time
        UnitCategory.Data -> data
        UnitCategory.Energy -> energy
        UnitCategory.Pressure -> pressure
    }

    /** Index of the unit a category should start on, paired with [defaultToIndex]. */
    fun defaultFromIndex(category: UnitCategory): Int = when (category) {
        UnitCategory.Length -> 2 // metre
        UnitCategory.Mass -> 2 // kilogram
        UnitCategory.Volume -> 1 // litre
        UnitCategory.Time -> 2 // minute
        UnitCategory.Data -> 4 // megabyte
        else -> 0
    }

    fun defaultToIndex(category: UnitCategory): Int = when (category) {
        UnitCategory.Length -> 4 // inch
        UnitCategory.Mass -> 5 // pound
        UnitCategory.Temperature -> 1 // fahrenheit
        UnitCategory.Volume -> 9 // gallon
        UnitCategory.Time -> 3 // hour
        UnitCategory.Data -> 6 // gigabyte
        else -> 1
    }
}

/** Converts [value] from one unit to another within the same category. */
fun convertUnits(value: Double, from: MeasureUnit, to: MeasureUnit): Double {
    val base = value * from.factor + from.offset
    return (base - to.offset) / to.factor
}
