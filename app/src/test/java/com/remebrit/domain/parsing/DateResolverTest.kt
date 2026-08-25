package com.remebrit.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateResolverTest {

    private val now = 1_735_000_000_000L

    @Test
    fun `tomorrow resolves to a later instant`() {
        val resolved = DateResolver.resolve("tomorrow", now)
        assertTrue(resolved != null && resolved > now)
    }

    @Test
    fun `unrecognised keyword resolves to null`() {
        assertEquals(null, DateResolver.resolve("someday", now))
    }
}