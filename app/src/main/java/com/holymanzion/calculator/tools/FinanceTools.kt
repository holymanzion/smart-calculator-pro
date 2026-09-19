package com.holymanzion.calculator.tools

import kotlin.math.pow

/**
 * Discount maths.
 *
 * Two discounts stack multiplicatively rather than adding up — 20% off then a further
 * 10% off is 28% off, not 30%. That distinction is the main reason this tool is worth
 * having, so [Result.effectiveDiscountPercent] reports what actually came off.
 */
object DiscountCalculator {

    data class Result(
        val discountedPrice: Double,
        val youSave: Double,
        val taxAmount: Double,
        val finalPrice: Double,
        val effectiveDiscountPercent: Double,
    )

    fun calculate(
        originalPrice: Double,
        discountPercent: Double,
        extraDiscountPercent: Double = 0.0,
        taxPercent: Double = 0.0,
    ): Result {
        val afterFirst = originalPrice * (1 - discountPercent / 100)
        val discounted = afterFirst * (1 - extraDiscountPercent / 100)
        val saved = originalPrice - discounted
        val tax = discounted * taxPercent / 100

        return Result(
            discountedPrice = discounted,
            youSave = saved,
            taxAmount = tax,
            finalPrice = discounted + tax,
            effectiveDiscountPercent = if (originalPrice == 0.0) 0.0 else saved / originalPrice * 100,
        )
    }
}

/**
 * Bill total with tax, tip and an even split.
 *
 * [tipOnPreTaxAmount] matters more than it looks: tipping on the post-tax total quietly
 * tips on the tax as well, so the default follows the pre-tax convention.
 */
object PriceCalculator {

    data class Result(
        val subtotal: Double,
        val taxAmount: Double,
        val tipAmount: Double,
        val total: Double,
        val perPerson: Double,
    )

    fun calculate(
        amount: Double,
        taxPercent: Double = 0.0,
        tipPercent: Double = 0.0,
        people: Int = 1,
        tipOnPreTaxAmount: Boolean = true,
    ): Result {
        val tax = amount * taxPercent / 100
        val tipBase = if (tipOnPreTaxAmount) amount else amount + tax
        val tip = tipBase * tipPercent / 100
        val total = amount + tax + tip
        val splitBetween = people.coerceAtLeast(1)

        return Result(
            subtotal = amount,
            taxAmount = tax,
            tipAmount = tip,
            total = total,
            perPerson = total / splitBetween,
        )
    }
}

/**
 * Amortised loan repayment (the standard EMI formula).
 *
 * `EMI = P·r·(1+r)^n / ((1+r)^n − 1)`, where `r` is the monthly rate.
 */
object LoanCalculator {

    data class Result(
        val monthlyPayment: Double,
        val totalInterest: Double,
        val totalPayable: Double,
    )

    fun calculate(principal: Double, annualRatePercent: Double, months: Int): Result {
        val n = months.coerceAtLeast(1)

        // A zero-rate loan is just the principal split evenly; the formula below
        // divides by zero there.
        if (annualRatePercent <= 0.0) {
            val payment = principal / n
            return Result(payment, 0.0, principal)
        }

        val r = annualRatePercent / 12 / 100
        val growth = (1 + r).pow(n)
        val payment = principal * r * growth / (growth - 1)
        val total = payment * n

        return Result(
            monthlyPayment = payment,
            totalInterest = total - principal,
            totalPayable = total,
        )
    }
}
