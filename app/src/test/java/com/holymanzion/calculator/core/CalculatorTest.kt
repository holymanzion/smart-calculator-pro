package com.holymanzion.calculator.core

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorTest {

    // ---- helpers ---------------------------------------------------------

    private fun eval(
        expression: String,
        mode: AngleMode = AngleMode.Degrees,
        ans: BigDecimal = BigDecimal.ZERO,
    ): String {
        val result = Calculator.evaluate(expression, mode, ans)
        assertTrue("expected '$expression' to evaluate, got $result", result is CalcResult.Success)
        return NumberFormatter.format((result as CalcResult.Success).value, grouped = false)
    }

    private fun errorOf(expression: String, mode: AngleMode = AngleMode.Degrees): CalcError {
        val result = Calculator.evaluate(expression, mode)
        assertTrue("expected '$expression' to fail, got $result", result is CalcResult.Failure)
        return (result as CalcResult.Failure).error
    }

    // ---- precedence and associativity ------------------------------------

    @Test
    fun `multiplication binds tighter than addition`() {
        assertEquals("14", eval("2+3*4"))
        assertEquals("20", eval("(2+3)*4"))
        assertEquals("50", eval("2+3*4^2"))
    }

    @Test
    fun `subtraction and division are left associative`() {
        assertEquals("5", eval("10-3-2"))
        assertEquals("5", eval("100/10/2"))
    }

    @Test
    fun `exponentiation is right associative`() {
        assertEquals("512", eval("2^3^2"))
    }

    @Test
    fun `unary minus applies to the whole power`() {
        assertEquals("-4", eval("-2^2"))
        assertEquals("4", eval("(-2)^2"))
    }

    @Test
    fun `negative exponents are supported`() {
        assertEquals("0.25", eval("2^-2"))
    }

    @Test
    fun `repeated unary signs collapse`() {
        assertEquals("3", eval("--3"))
        assertEquals("-3", eval("-+3"))
    }

    // ---- decimal behaviour -----------------------------------------------

    @Test
    fun `decimal addition does not leak binary floating point error`() {
        assertEquals("0.3", eval("0.1+0.2"))
        assertEquals("0.3", eval("0.7-0.4"))
    }

    @Test
    fun `repeating division is rounded to display precision`() {
        assertEquals("0.333333333333", eval("1/3"))
    }

    @Test
    fun `thousands separators in input are ignored`() {
        assertEquals("1001", eval("1,000+1"))
    }

    @Test
    fun `scientific notation literals parse`() {
        assertEquals("1500", eval("1.5E3"))
        assertEquals("0.0015", eval("1.5E-3"))
    }

    @Test
    fun `uppercase E is an exponent and lowercase e is Euler's number`() {
        assertEquals("2.71828182846", eval("e"))
        assertEquals("2000", eval("2E3"))
    }

    // ---- percent ---------------------------------------------------------

    @Test
    fun `percent after plus or minus is relative to the left operand`() {
        assertEquals("220", eval("200+10%"))
        assertEquals("180", eval("200-10%"))
    }

    @Test
    fun `percent elsewhere is just a division by one hundred`() {
        assertEquals("20", eval("200*10%"))
        assertEquals("2000", eval("200/10%"))
        assertEquals("0.5", eval("50%"))
        assertEquals("5.1", eval("10%+5"))
    }

    @Test
    fun `chained percentages compound against the running total`() {
        assertEquals("110.25", eval("100+5%+5%"))
    }

    // ---- implicit multiplication -----------------------------------------

    @Test
    fun `juxtaposition means multiplication`() {
        assertEquals("14", eval("2(3+4)"))
        assertEquals("21", eval("(1+2)(3+4)"))
        assertEquals("6.28318530718", eval("2π"))
        assertEquals("6", eval("3sqrt(4)"))
    }

    // ---- functions -------------------------------------------------------

    @Test
    fun `roots and logarithms`() {
        assertEquals("4", eval("sqrt(16)"))
        assertEquals("3", eval("√9"))
        assertEquals("2", eval("sqrt(sqrt(16))"))
        assertEquals("1.41421356237", eval("sqrt(2)"))
        assertEquals("1", eval("ln(e)"))
        assertEquals("3", eval("log(1000)"))
        assertEquals("3", eval("log2(8)"))
        assertEquals("-2", eval("cbrt(-8)"))
    }

    @Test
    fun `rounding functions`() {
        assertEquals("2", eval("floor(2.7)"))
        assertEquals("3", eval("ceil(2.1)"))
        assertEquals("3", eval("round(2.5)"))
        assertEquals("-3", eval("floor(-2.1)"))
        assertEquals("7", eval("abs(-7)"))
    }

    @Test
    fun `factorial`() {
        assertEquals("1", eval("0!"))
        assertEquals("120", eval("5!"))
        assertEquals("3628800", eval("10!"))
    }

    // ---- trigonometry ----------------------------------------------------

    @Test
    fun `degree mode trigonometry`() {
        assertEquals("0.5", eval("sin(30)"))
        assertEquals("0.5", eval("cos(60)"))
        assertEquals("1", eval("tan(45)"))
    }

    @Test
    fun `quadrant boundaries are exact in degree mode`() {
        assertEquals("0", eval("sin(180)"))
        assertEquals("0", eval("cos(90)"))
        assertEquals("-1", eval("cos(180)"))
        assertEquals("1", eval("sin(90)"))
        assertEquals("-1", eval("sin(-90)"))
        assertEquals("0", eval("tan(180)"))
    }

    @Test
    fun `radian mode trigonometry`() {
        assertEquals("0", eval("sin(0)", AngleMode.Radians))
        assertEquals("1", eval("cos(0)", AngleMode.Radians))
        assertEquals("1", eval("sin(π/2)", AngleMode.Radians))
    }

    @Test
    fun `inverse trigonometry respects the angle mode`() {
        assertEquals("30", eval("asin(0.5)"))
        assertEquals("45", eval("atan(1)"))
        assertEquals("0.523598775598", eval("asin(0.5)", AngleMode.Radians))
    }

    // ---- error handling --------------------------------------------------

    @Test
    fun `division by zero is reported`() {
        assertEquals(CalcError.DivisionByZero, errorOf("1/0"))
        assertEquals(CalcError.DivisionByZero, errorOf("0^-1"))
    }

    @Test
    fun `domain errors are reported`() {
        assertEquals(CalcError.Domain, errorOf("sqrt(-1)"))
        assertEquals(CalcError.Domain, errorOf("ln(0)"))
        assertEquals(CalcError.Domain, errorOf("ln(-1)"))
        assertEquals(CalcError.Domain, errorOf("asin(2)"))
        assertEquals(CalcError.Domain, errorOf("tan(90)"))
        assertEquals(CalcError.Domain, errorOf("(-1)!"))
        assertEquals(CalcError.Domain, errorOf("2.5!"))
        assertEquals(CalcError.Domain, errorOf("(-8)^0.5"))
    }

    @Test
    fun `syntax errors are reported`() {
        assertEquals(CalcError.Syntax, errorOf("2+"))
        assertEquals(CalcError.Syntax, errorOf("*5"))
        assertEquals(CalcError.Syntax, errorOf("foo(2)"))
        assertEquals(CalcError.Syntax, errorOf("sin"))
    }

    @Test
    fun `unbalanced parentheses are reported`() {
        assertEquals(CalcError.UnbalancedParens, errorOf("(2+3"))
        assertEquals(CalcError.UnbalancedParens, errorOf("2+3)"))
    }

    @Test
    fun `empty input is not an error state`() {
        assertEquals(CalcError.Empty, errorOf(""))
        assertEquals(CalcError.Empty, errorOf("   "))
    }

    @Test
    fun `absurd magnitudes overflow rather than hang`() {
        assertEquals(CalcError.Overflow, errorOf("1E999999999"))
        assertEquals(CalcError.Overflow, errorOf("1001!"))
        assertEquals(CalcError.Overflow, errorOf("9^9^9"))
    }

    // ---- ans -------------------------------------------------------------

    @Test
    fun `ans substitutes the previous result`() {
        assertEquals("12", eval("ans*3", ans = BigDecimal(4)))
        assertEquals("10", eval("ans+ans", ans = BigDecimal(5)))
    }

    // ---- formatting ------------------------------------------------------

    @Test
    fun `large and small magnitudes switch to scientific notation`() {
        val big = eval("99^99")
        assertTrue("expected scientific notation, got $big", big.endsWith("E197"))
        assertTrue("expected leading digits 3.69, got $big", big.startsWith("3.69"))

        assertEquals("1E-9", eval("0.000000001"))
        assertEquals("0.0001", eval("0.0001"))
    }

    @Test
    fun `thousands separators are added to results`() {
        val value = (Calculator.evaluate("1000000") as CalcResult.Success).value
        assertEquals("1,000,000", NumberFormatter.format(value))
        assertEquals("1000000", NumberFormatter.format(value, grouped = false))

        val negative = (Calculator.evaluate("-1234567.5") as CalcResult.Success).value
        assertEquals("-1,234,567.5", NumberFormatter.format(negative))
    }

    @Test
    fun `zero always formats as a bare zero`() {
        assertEquals("0", eval("0"))
        assertEquals("0", eval("5-5"))
        assertEquals("0", eval("0.0"))
    }

    // ---- preview ---------------------------------------------------------

    @Test
    fun `preview returns null for incomplete input instead of an error`() {
        assertNull(Calculator.preview("2+"))
        assertNull(Calculator.preview(""))
        assertEquals("5", Calculator.preview("2+3"))
    }
}
