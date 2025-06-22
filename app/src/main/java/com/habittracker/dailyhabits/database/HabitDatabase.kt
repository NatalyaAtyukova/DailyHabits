package com.habittracker.dailyhabits.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habittracker.dailyhabits.model.Habit

@Database(entities = [Habit::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN type TEXT NOT NULL DEFAULT 'SIMPLE'")
                db.execSQL("ALTER TABLE habits ADD COLUMN targetValue REAL")
                db.execSQL("ALTER TABLE habits ADD COLUMN unit TEXT")
                db.execSQL("ALTER TABLE habits ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderTime TEXT")
                db.execSQL("ALTER TABLE habits ADD COLUMN repeatDays TEXT NOT NULL DEFAULT '[]'")

                // Преобразование dailyStatus из Map<Long, Boolean> в Map<Long, Float>
                // Мы читаем старые данные, конвертируем их и записываем обратно.
                // Этот код просто заменяет старые boolean значения на 0.0 или 1.0
                db.execSQL("UPDATE habits SET dailyStatus = REPLACE(REPLACE(dailyStatus, 'true', '1.0'), 'false', '0.0')")
            }
        }

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearDatabase(context: Context) {
            context.deleteDatabase("habit_database")
        }
    }
}