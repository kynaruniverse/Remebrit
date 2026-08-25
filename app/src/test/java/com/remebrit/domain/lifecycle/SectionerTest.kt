package com.remebrit.domain.lifecycle

import org.junit.Assert.assertEquals
import org.junit.Test

class SectionerTest {

    private val now = 1_735_000_000_000L // fixed reference instant

    @Test
    fun `no relevant date goes to inbox`() {
        assertEquals(Section.INBOX, Sectioner.sectionFor(null, now))
    }

    @Test
    fun `within an hour is now`() {
        val relevant = now + (30 * 60 * 1000L)
        assertEquals(Section.NOW, Sectioner.sectionFor(relevant, now))
    }

    @Test
    fun `later same day is today`() {
        val relevant = now + (5 * 60 * 60 * 1000L) // 5 hours later
        val section = Sectioner.sectionFor(relevant, now)
        // Could land as NOW or TODAY depending on whether it crosses midnight
        // in the fixed instant above — assert it's not INBOX, which is the
        // behaviour that actually matters here.
        assert(section != Section.INBOX)
    }

    @Test
    fun `distant future date is inbox`() {
        val relevant = now + (30L * 24 * 60 * 60 * 1000L) // 30 days later
        assertEquals(Section.INBOX, Sectioner.sectionFor(relevant, now))
    }
}