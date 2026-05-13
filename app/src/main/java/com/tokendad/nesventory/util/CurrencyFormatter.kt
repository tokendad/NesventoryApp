package com.tokendad.nesventory.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val currencyFormat: NumberFormat
        get() = NumberFormat.getCurrencyInstance(Locale.getDefault())

    fun format(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            val amount = value.toDoubleOrNull() ?: return "$$value"
            currencyFormat.format(amount)
        } catch (e: Exception) {
            "$$value"
        }
    }

    fun format(value: Double?): String {
        if (value == null) return ""
        return currencyFormat.format(value)
    }
}
