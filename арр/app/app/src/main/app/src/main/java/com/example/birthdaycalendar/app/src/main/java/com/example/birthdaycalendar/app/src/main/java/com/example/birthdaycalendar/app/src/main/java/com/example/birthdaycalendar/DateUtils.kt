package com.example.birthdaycalendar

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateUtils {

    fun nextBirthday(day: Int, month: Int): LocalDate {
        val today = LocalDate.now()
        var next = safeDate(today.year, month, day)
        if (next.isBefore(today)) {
            next = safeDate(today.year + 1, month, day)
        }
        return next
    }

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate =
        try {
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            LocalDate.of(year, month, 28)
        }

    fun daysUntil(day: Int, month: Int): Long =
        ChronoUnit.DAYS.between(LocalDate.now(), nextBirthday(day, month))

    fun ageTurning(birthYear: Int?, day: Int, month: Int): Int? {
        if (birthYear == null || birthYear <= 0) return null
        return nextBirthday(day, month).year - birthYear
    }
}
