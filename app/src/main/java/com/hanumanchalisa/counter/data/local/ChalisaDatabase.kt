package com.hanumanchalisa.counter.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The app's SQLite database.
 *
 * Room stores the file in the app's private data directory on internal storage, which is what makes
 * the counts survive the app being closed or swiped away, and the phone being restarted. Nothing is
 * held in memory only, and no cache directory is used.
 */
@Database(
    entities = [DayCountEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ChalisaDatabase : RoomDatabase() {

    abstract fun dayCountDao(): DayCountDao

    companion object {
        private const val DATABASE_NAME = "hanuman_chalisa.db"

        fun create(context: Context): ChalisaDatabase =
            Room.databaseBuilder(
                context = context.applicationContext,
                klass = ChalisaDatabase::class.java,
                name = DATABASE_NAME,
            ).build()
    }
}
