package com.holymanzion.calculator.tools

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BaseConverterTest {

    @Test
    fun `converts between all four bases`() {
        assertEquals("FF", BaseConverter.convert("255", NumberBase.Decimal, NumberBase.Hexadecimal))
        assertEquals("255", BaseConverter.convert("FF", NumberBase.Hexadecimal, NumberBase.Decimal))
        assertEquals("11111111", BaseConverter.convert("FF", NumberBase.Hexadecimal, NumberBase.Binary))
        assertEquals("377", BaseConverter.convert("255", NumberBase.Decimal, NumberBase.Octal))
        assertEquals("1010", BaseConverter.convert("10", NumberBase.Decimal, NumberBase.Binary))
    }

    @Test
    fun `round trips through every base`() {
        NumberBase.entries.forEach { target ->
            val encoded = BaseConverter.convert("48879", NumberBase.Decimal, target)!!
            val back = BaseConverter.convert(encoded, target, NumberBase.Decimal)
            assertEquals("round trip via ${target.label}", "48879", back)
        }
    }

    @Test
    fun `hex input is case insensitive`() {
        assertEquals("48879", BaseConverter.convert("beef", NumberBase.Hexadecimal, NumberBase.Decimal))
        assertEquals("48879", BaseConverter.convert("BEEF", NumberBase.Hexadecimal, NumberBase.Decimal))
    }

    @Test
    fun `a full 64 bit value does not wrap negative`() {
        // The classic base-converter bug: parsed as a signed Long this is -1.
        val max = "FFFFFFFFFFFFFFFF"
        val decimal = BaseConverter.convert(max, NumberBase.Hexadecimal, NumberBase.Decimal)
        assertEquals("18446744073709551615", decimal)
    }

    @Test
    fun `values far beyond 64 bits still convert`() {
        val huge = BigInteger.TWO.pow(200).toString()
        val hex = BaseConverter.convert(huge, NumberBase.Decimal, NumberBase.Hexadecimal)!!
        assertEquals(huge, BaseConverter.convert(hex, NumberBase.Hexadecimal, NumberBase.Decimal))
    }

    @Test
    fun `negative numbers are preserved`() {
        assertEquals("-FF", BaseConverter.convert("-255", NumberBase.Decimal, NumberBase.Hexadecimal))
        assertEquals("-255", BaseConverter.convert("-FF", NumberBase.Hexadecimal, NumberBase.Decimal))
    }

    @Test
    fun `digits outside the base are rejected`() {
        assertNull(BaseConverter.parse("2", NumberBase.Binary))
        assertNull(BaseConverter.parse("8", NumberBase.Octal))
        assertNull(BaseConverter.parse("G", NumberBase.Hexadecimal))
        assertNull(BaseConverter.parse("A", NumberBase.Decimal))
    }

    @Test
    fun `empty input parses to nothing`() {
        assertNull(BaseConverter.parse("", NumberBase.Decimal))
        assertNull(BaseConverter.parse("   ", NumberBase.Decimal))
    }

    @Test
    fun `zero converts in every base`() {
        NumberBase.entries.forEach { base ->
            assertEquals("0", BaseConverter.convert("0", NumberBase.Decimal, base))
        }
    }

    @Test
    fun `binary groups in fours and the rest in threes`() {
        assertEquals("1111 1111", BaseConverter.group("11111111", NumberBase.Binary))
        assertEquals("1 000 000", BaseConverter.group("1000000", NumberBase.Decimal))
        assertEquals("FF", BaseConverter.group("FF", NumberBase.Hexadecimal))
        assertEquals("-1 234", BaseConverter.group("-1234", NumberBase.Decimal))
    }

    @Test
    fun `bit length reports what the value needs`() {
        assertEquals(8, BaseConverter.bitLength(BigInteger.valueOf(255)))
        assertEquals(9, BaseConverter.bitLength(BigInteger.valueOf(256)))
        assertEquals(64, BaseConverter.bitLength(BigInteger("18446744073709551615")))
        // Zero still occupies a bit as far as a reader is concerned.
        assertEquals(1, BaseConverter.bitLength(BigInteger.ZERO))
    }

    @Test
    fun `each base accepts only its own digits`() {
        assertEquals("01", NumberBase.Binary.digits)
        assertEquals("01234567", NumberBase.Octal.digits)
        assertEquals("0123456789", NumberBase.Decimal.digits)
        assertEquals("0123456789ABCDEF", NumberBase.Hexadecimal.digits)
    }
}
