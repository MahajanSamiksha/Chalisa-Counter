package com.hanumanchalisa.counter.domain.model

/**
 * Configuration of a sadhana (spiritual practice) cycle.
 *
 * Keeping these values as injected data rather than hardcoded constants is what makes the app
 * Open/Closed: a different practice — say 21 days with a target of 50 — needs no code change,
 * only a different [SadhanaConfig] instance.
 */
data class SadhanaConfig(
    val totalDays: Int,
    val targetCount: Int,
) {
    init {
        require(totalDays > 0) { "totalDays must be positive, was $totalDays" }
        require(targetCount > 0) { "targetCount must be positive, was $targetCount" }
    }

    /** Inclusive range of valid day numbers, i.e. 1..totalDays. */
    val dayRange: IntRange get() = 1..totalDays

    fun isValidDay(day: Int): Boolean = day in dayRange

    companion object {
        /** The Hanuman Chalisa practice this app was built for: 100 recitations across 40 days. */
        val HANUMAN_CHALISA_40_DAYS = SadhanaConfig(totalDays = 40, targetCount = 100)
    }
}
