package com.hanumanchalisa.counter.domain.model

/**
 * A complete snapshot of the practice: every day of the cycle plus the derived totals.
 *
 * [days] always contains exactly [SadhanaConfig.totalDays] entries in ascending day order, so the
 * UI can render the full Day 1 … Day 40 list straight from this value without filling gaps itself.
 *
 * Totals are computed here rather than stored, so they can never drift out of sync with [days].
 */
data class SadhanaProgress(
    val days: List<DayProgress>,
    val config: SadhanaConfig,
) {
    /** Sum of recitations across every day of the cycle. */
    val totalCount: Int = days.sumOf { it.count }

    /** The goal being worked toward, e.g. 100. */
    val targetCount: Int get() = config.targetCount

    /** True once the target has been reached (or exceeded). */
    val isTargetReached: Boolean get() = totalCount >= targetCount

    /**
     * Progress toward the target as a fraction in 0f..1f, suitable for a progress bar.
     * Clamped at 1f so exceeding the target does not overflow the indicator.
     */
    val completionFraction: Float
        get() = (totalCount.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f)

    /** How many recitations remain before the target is met; 0 once reached. */
    val remainingCount: Int get() = (targetCount - totalCount).coerceAtLeast(0)

    companion object {
        /** A progress snapshot where every day of [config] is present with a count of zero. */
        fun empty(config: SadhanaConfig): SadhanaProgress = SadhanaProgress(
            days = config.dayRange.map { DayProgress(day = it, count = 0) },
            config = config,
        )
    }
}
