package com.remebrit.domain.parsing

enum class EntityType { DATE, TIME, URL, MONEY, TASK }
enum class Confidence { HIGH, MEDIUM, LOW }

data class ParsedEntity(
    val type: EntityType,
    val value: String,
    val confidence: Confidence
)

/**
 * Deterministic, local-only parsing. No AI, no network.
 * Golden rule (Part 2.4 of the spec): never make the user correct an
 * interpretation they never asked for. When unsure, detect nothing.
 */
object CaptureParser {

    private val urlRegex = Regex("""(https?://\S+|www\.\S+)""", RegexOption.IGNORE_CASE)
    private val moneyRegex = Regex("""[£$€]\s?\d+(\.\d{2})?""")
    private val timeRegex = Regex("""\b\d{1,2}(:\d{2})?\s?(am|pm)\b|\b\d{1,2}:\d{2}\b""", RegexOption.IGNORE_CASE)

    private val dateKeywords = listOf(
        "today", "tonight", "tomorrow", "next week", "next month",
        "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"
    )

    private val taskVerbs = listOf(
        "buy", "call", "book", "send", "check", "return",
        "remember to", "pick up", "renew", "ask"
    )

    fun parse(content: String): List<ParsedEntity> {
        val lower = content.lowercase()
        val entities = mutableListOf<ParsedEntity>()

        urlRegex.findAll(content).forEach {
            entities += ParsedEntity(EntityType.URL, it.value, Confidence.HIGH)
        }
        moneyRegex.findAll(content).forEach {
            entities += ParsedEntity(EntityType.MONEY, it.value, Confidence.HIGH)
        }
        timeRegex.findAll(content).forEach {
            entities += ParsedEntity(EntityType.TIME, it.value, Confidence.HIGH)
        }
        dateKeywords.forEach { keyword ->
            if (lower.contains(keyword)) {
                entities += ParsedEntity(EntityType.DATE, keyword, Confidence.HIGH)
            }
        }
        taskVerbs.forEach { verb ->
            if (lower.contains(verb)) {
                entities += ParsedEntity(EntityType.TASK, verb, Confidence.MEDIUM)
            }
        }

        return entities
    }
}