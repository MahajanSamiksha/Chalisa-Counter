package com.hanumanchalisa.counter.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A persisted recitation count for one day, stored as a row in SQLite.
 *
 * The day number is the primary key, so each day can appear at most once and an upsert on that key
 * is enough to add a count without first reading it back.
 *
 * This type belongs to the data layer only — the rest of the app works with the domain's
 * [com.hanumanchalisa.counter.domain.model.DayProgress] instead, so the storage schema can change
 * independently of the app's logic.
 */
@Entity(tableName = "day_counts")
data class DayCountEntity(
    @PrimaryKey
    @ColumnInfo(name = "day")
    val day: Int,

    @ColumnInfo(name = "count")
    val count: Int,
)
