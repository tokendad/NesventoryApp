package com.tokendad.nesventorynew.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val outputDateFormat: DateFormat
        get() = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    private val outputDateTimeFormat: DateFormat
        get() = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())

    fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val date = try {
                inputFormat.parse(isoDate)
            } catch (e: Exception) {
                dateOnlyFormat.parse(isoDate)
            }
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
