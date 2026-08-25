package com.remebrit.domain.parsing

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Resolves a detected date keyword into an actual instant, so the
 * Sectioner (NOW/TODAY/INBOX) has something concrete to sort by.
 * Deliberately simple for MVP: defaults to 9am local time on the
 * resolved date. Combining this with detected TIME entities for a
 * precise hour is a follow-up refinement, not needed for this slice.
 */
object DateResolver {

    private val weekdays = mapOf(
        "monday" to DayOfWeek.MONDAY, "tuesday" to DayOfWeek.TUESDAY,
        "wednesday" to DayOfWeek.WEDNESDAY, "thursday" to DayOfWeek.THURSDAY,
        "friday" to DayOfWeek.FRIDAY, "saturday" to DayOfWeek.SATURDAY,
        "sunday" to DayOfWeek.SUNDAY
    )

    fun resolve(keyword: String, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long? {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

        val date: LocalDate = when (keyword.lowercase()) {
            "today", "tonight" -> today
            "tomorrow" -> today.plusDays(1)
            "next week" -> today.plusWeeks(1)
            "next month" -> today.plusMonths(1)
            else -> weekdays[keyword.lowercase()]?.let { target ->
                var d = today
                do { d = d.plusDays(1) } while (d.dayOfWeek != target)
                d
            } ?: return null
        }

        return date.atStartOfDay(zone).plusHours(9).toInstant().toEpochMilli()
    }
}