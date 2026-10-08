package com.hanumanchalisa.counter.domain.importing

import com.hanumanchalisa.counter.domain.model.DayProgress

/**
 * Exactly what an import will do, worked out before anything is written so the user can confirm it.
 *
 * The import rule is: every day with a count in the file takes the file's count; every other day
 * keeps the count already on this phone. Zeros in the file mean "nothing recorded" and never clear a
 * count. Because the file's counts are *set* rather than added, importing the same file twice gives
 * the same result as importing it once.
 *
 * @property restoredDays days with a count above zero in the file — these are what gets written.
 * @property keptDays days counted on this phone that the file has no count for; left untouched.
 * @property replacedDays days counted both here and in the file with different values; the file's
 *   count wins, so the user is warned about each one.
 * @property totalAfterImport the overall total once the import is applied.
 * @property targetCount the practice target, for the "N of 100" summary.
 * @property changesNothing true when every restored day already has that exact count on this phone.
 */
data class ImportPreview(
    val restoredDays: List<DayProgress>,
    val keptDays: List<DayProgress>,
    val replacedDays: List<DayReplacement>,
    val totalAfterImport: Int,
    val targetCount: Int,
    val changesNothing: Boolean,
) {
    /** Recitations being brought in from the file. */
    val restoredTotal: Int get() = restoredDays.sumOf { it.count }

    /** Recitations on this phone that stay exactly as they are. */
    val keptTotal: Int get() = keptDays.sumOf { it.count }
}

/** A day whose count on this phone, [currentCount], will become [importedCount] from the file. */
data class DayReplacement(
    val day: Int,
    val currentCount: Int,
    val importedCount: Int,
)
