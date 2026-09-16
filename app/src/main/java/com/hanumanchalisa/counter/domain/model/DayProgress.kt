package com.hanumanchalisa.counter.domain.model

/**
 * The recitation count recorded for a single day of the practice.
 *
 * Immutable by design: changing a count produces a new instance rather than mutating this one,
 * which keeps Compose's state comparison correct and makes the type safe to share across threads.
 *
 * @property day 1-based day number within the cycle (Day 1 … Day 40).
 * @property count number of recitations recorded on that day; never negative.
 */
data class DayProgress(
    val day: Int,
    val count: Int,
) {
    init {
        require(day >= 1) { "day must be 1-based, was $day" }
        require(count >= 0) { "count cannot be negative, was $count" }
    }

    /** True when this day has at least one recitation recorded. */
    val hasRecitations: Boolean get() = count > 0
}
