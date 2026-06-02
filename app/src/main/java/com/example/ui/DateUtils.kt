package com.example.ui

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatDate(timeInMillis: Long): String {
        if (timeInMillis <= 0) return "Not Set"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun formatDateShort(timeInMillis: Long): String {
        if (timeInMillis <= 0) return "N/A"
        val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun getDaysRemaining(targetTimeInMillis: Long): Long {
        if (targetTimeInMillis <= 0) return -1
        val diff = targetTimeInMillis - System.currentTimeMillis()
        // If it's today, we return 0. If expired, it's negative number of days
        return diff / (1000 * 60 * 60 * 24)
    }

    fun getYearMonthKey(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun formatYearMonthKey(key: String): String {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = sdfInput.parse(key)
            if (date != null) {
                return sdfOutput.format(date)
            }
        } catch (e: Exception) {
            // fallback
        }
        return key
    }
}
