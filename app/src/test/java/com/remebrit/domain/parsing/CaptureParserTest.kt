package com.remebrit.domain.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureParserTest {

    @Test
    fun `detects date keyword`() {
        val result = CaptureParser.parse("Buy milk tomorrow")
        assertTrue(result.any { it.type == EntityType.DATE && it.value == "tomorrow" })
    }

    @Test
    fun `detects task verb as medium confidence`() {
        val result = CaptureParser.parse("Buy milk tomorrow")
        val task = result.first { it.type == EntityType.TASK }
        assertEquals(Confidence.MEDIUM, task.confidence)
    }

    @Test
    fun `detects money as high confidence`() {
        val result = CaptureParser.parse("£39.99 jacket")
        val money = result.first { it.type == EntityType.MONEY }
        assertEquals("£39.99", money.value)
        assertEquals(Confidence.HIGH, money.confidence)
    }

    @Test
    fun `detects url`() {
        val result = CaptureParser.parse("Check this https://example.com/part")
        assertTrue(result.any { it.type == EntityType.URL })
    }

    @Test
    fun `plain unrecognisable text produces no entities`() {
        val result = CaptureParser.parse("Parking C17")
        assertTrue(result.isEmpty())
    }
}