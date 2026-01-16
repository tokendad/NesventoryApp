package com.tokendad.nesventorynew.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val outputDateFormat: DateFormat by lazy {
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    }

    private val outputDateTimeFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    }

    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = inputFormat.parse(isoDate) ?: dateOnlyFormat.parse(isoDate)
            date?.let { outputDateFormat.format(it) } ?: isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatDateTime(isoDateTime: String?): String {
        if (isoDateTime.isNullOrBlank()) return ""
        return try {
            val date = inputFormat.parse(isoDateTime)
            date?.let { outputDateTimeFormat.format(it) } ?: isoDateTime
        } catch (e: Exception) {
            isoDateTime
        }
    }
}
