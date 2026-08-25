package com.remebrit.domain.lifecycle

import java.time.Instant
import java.time.ZoneId

enum class Section { NOW, TODAY, INBOX }

/**
 * Decides which Home section an item belongs in, based purely on
 * relevantAt vs. the current time. No Android dependency — testable
 * as plain JVM code. This is the logic flagged as the highest-risk
 * part of Home (Part 7.2 of the spec), so it lives here, isolated
 * and unit-tested, before any UI touches it.
 */
object Sectioner {

    private const val NOW_WINDOW_MS = 60 * 60 * 1000L // within 1 hour = "NOW"

    fun sectionFor(
        relevantAtMillis: Long?,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): Section {
        if (relevantAtMillis == null) return Section.INBOX

        val diff = relevantAtMillis - nowMillis
        if (diff in -NOW_WINDOW_MS..NOW_WINDOW_MS) return Section.NOW

        val relevantDate = Instant.ofEpochMilli(relevantAtMillis).atZone(zone).toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

        return if (relevantDate == today) Section.TODAY else Section.INBOX
    }
}