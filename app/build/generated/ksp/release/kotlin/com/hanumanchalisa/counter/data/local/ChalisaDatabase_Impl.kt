package com.hanumanchalisa.counter.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChalisaDatabase_Impl : ChalisaDatabase() {
  private val _dayCountDao: Lazy<DayCountDao> = lazy {
    DayCountDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "6832f0a489d9aa9006795e6d883f4c94", "d639329eb9b1e5ce2ac4e10dd55b48ab") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `day_counts` (`day` INTEGER NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`day`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6832f0a489d9aa9006795e6d883f4c94')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `day_counts`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsDayCounts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDayCounts.put("day", TableInfo.Column("day", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDayCounts.put("count", TableInfo.Column("count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDayCounts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDayCounts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDayCounts: TableInfo = TableInfo("day_counts", _columnsDayCounts, _foreignKeysDayCounts, _indicesDayCounts)
        val _existingDayCounts: TableInfo = read(connection, "day_counts")
        if (!_infoDayCounts.equals(_existingDayCounts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |day_counts(com.hanumanchalisa.counter.data.local.DayCountEntity).
              | Expected:
              |""".trimMargin() + _infoDayCounts + """
              |
              | Found:
              |""".trimMargin() + _existingDayCounts)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "day_counts")
  }

  public override fun clearAllTables() {
    super.performClear(false, "day_counts")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(DayCountDao::class, DayCountDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun dayCountDao(): DayCountDao = _dayCountDao.value
}
