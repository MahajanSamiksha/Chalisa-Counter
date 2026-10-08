package com.hanumanchalisa.counter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * SQL access to the `day_counts` table. Its only responsibility is talking to the database.
 *
 * Counts are changed with SQL arithmetic (`count + 1`) rather than by reading a value into Kotlin,
 * modifying it and writing it back. That matters for a tap-counter: two fast taps can never read the
 * same starting value and lose one of the increments.
 *
 * Increment is expressed as `INSERT OR IGNORE` followed by `UPDATE` inside a [Transaction] rather
 * than as a single `ON CONFLICT ... DO UPDATE` upsert, because SQLite only gained upsert support in
 * 3.24 (Android API 30) and this app supports API 24.
 */
@Dao
interface DayCountDao {

    /** Every recorded day, ordered by day number, re-emitting on any change to the table. */
    @Query("SELECT * FROM day_counts ORDER BY day ASC")
    fun observeAll(): Flow<List<DayCountEntity>>

    /** Creates a zero row for [day] if it is not already present. */
    @Query("INSERT OR IGNORE INTO day_counts (day, `count`) VALUES (:day, 0)")
    suspend fun insertZeroIfAbsent(day: Int)

    @Query("UPDATE day_counts SET `count` = `count` + 1 WHERE day = :day")
    suspend fun incrementExisting(day: Int)

    /** Decrements only while above zero, so a count can never become negative. */
    @Query("UPDATE day_counts SET `count` = `count` - 1 WHERE day = :day AND `count` > 0")
    suspend fun decrementExisting(day: Int)

    /**
     * Writes each row's count, overwriting any existing row for that day. Room runs a list insert in
     * a single transaction, so an import is applied completely or not at all. `REPLACE` is plain
     * `INSERT OR REPLACE`, which every supported SQLite version understands.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<DayCountEntity>)

    @Query("DELETE FROM day_counts")
    suspend fun deleteAll()

    /**
     * Adds one recitation to [day], creating the row when this is the day's first recitation.
     * Both statements run in one transaction, so the increment is atomic.
     */
    @Transaction
    suspend fun increment(day: Int) {
        insertZeroIfAbsent(day)
        incrementExisting(day)
    }

    /** Removes one recitation from [day]; a no-op when the day is absent or already zero. */
    @Transaction
    suspend fun decrement(day: Int) {
        decrementExisting(day)
    }
}
