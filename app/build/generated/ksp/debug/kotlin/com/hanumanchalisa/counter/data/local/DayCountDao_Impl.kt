package com.hanumanchalisa.counter.`data`.local

import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DayCountDao_Impl(
  __db: RoomDatabase,
) : DayCountDao {
  private val __db: RoomDatabase
  init {
    this.__db = __db
  }

  public override suspend fun increment(day: Int): Unit = performInTransactionSuspending(__db) {
    super@DayCountDao_Impl.increment(day)
  }

  public override suspend fun decrement(day: Int): Unit = performInTransactionSuspending(__db) {
    super@DayCountDao_Impl.decrement(day)
  }

  public override fun observeAll(): Flow<List<DayCountEntity>> {
    val _sql: String = "SELECT * FROM day_counts ORDER BY day ASC"
    return createFlow(__db, false, arrayOf("day_counts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDay: Int = getColumnIndexOrThrow(_stmt, "day")
        val _columnIndexOfCount: Int = getColumnIndexOrThrow(_stmt, "count")
        val _result: MutableList<DayCountEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DayCountEntity
          val _tmpDay: Int
          _tmpDay = _stmt.getLong(_columnIndexOfDay).toInt()
          val _tmpCount: Int
          _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          _item = DayCountEntity(_tmpDay,_tmpCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun insertZeroIfAbsent(day: Int) {
    val _sql: String = "INSERT OR IGNORE INTO day_counts (day, `count`) VALUES (?, 0)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun incrementExisting(day: Int) {
    val _sql: String = "UPDATE day_counts SET `count` = `count` + 1 WHERE day = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun decrementExisting(day: Int) {
    val _sql: String = "UPDATE day_counts SET `count` = `count` - 1 WHERE day = ? AND `count` > 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM day_counts"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
