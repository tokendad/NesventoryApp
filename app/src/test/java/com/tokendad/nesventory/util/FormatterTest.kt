package com.tokendad.nesventory.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class FormatterTest {

    @Test
    fun currencyFormatter_formatsStringCorrectly() {
        // Note: Formatting depends on the Locale.getDefault(). 
        // In a real test environment, we might want to enforce a locale or mock it.
        // For now, we assume US or generic usually works for basic validation, 
        // or check if it starts with the symbol.
        
        // Let's enforce US locale for this test to be deterministic if possible, 
        // but Locale.setDefault() affects the whole JVM.
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            
            assertEquals("$10.00", CurrencyFormatter.format("10"))
            assertEquals("$10.50", CurrencyFormatter.format("10.5"))
            assertEquals("$1,000.00", CurrencyFormatter.format("1000"))
            assertEquals("$0.00", CurrencyFormatter.format("0"))
            assertEquals("", CurrencyFormatter.format(""))
            assertEquals("", CurrencyFormatter.format(null as String?))
            // Invalid input should return "$input"
            assertEquals("\$invalid", CurrencyFormatter.format("invalid"))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun currencyFormatter_formatsDoubleCorrectly() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            
            assertEquals("$10.00", CurrencyFormatter.format(10.0))
            assertEquals("$10.50", CurrencyFormatter.format(10.5))
            assertEquals("$1,000.00", CurrencyFormatter.format(1000.0))
            assertEquals("$0.00", CurrencyFormatter.format(0.0))
            assertEquals("", CurrencyFormatter.format(null as Double?))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun dateFormatter_formatsDateCorrectly() {
        // DateFormatter uses DateFormat.getDateInstance(DateFormat.MEDIUM)
        // In US Locale: "Jan 1, 2024" or similar
        
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            
            // ISO Date Only
            val inputDate = "2024-01-01"
            val formattedDate = DateFormatter.formatDate(inputDate)
            // Exact format might vary slightly by JDK version (e.g. "Jan 1, 2024"), 
            // so checking it's not the original ISO string is a good start, 
            // or checking basic components.
            assert(formattedDate.contains("2024"))
            assert(formattedDate.contains("Jan"))
            
            // ISO Date Time
            val inputDateTime = "2024-01-01T12:00:00"
            val formattedDateTime = DateFormatter.formatDate(inputDateTime)
            assert(formattedDateTime.contains("2024"))
            assert(formattedDateTime.contains("Jan"))
            
            // Invalid
            assertEquals("invalid", DateFormatter.formatDate("invalid"))
            assertEquals("", DateFormatter.formatDate(null))
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
