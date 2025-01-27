package com.habittracker.dailyhabits.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.habittracker.dailyhabits.model.Habit

@Database(entities = [Habit::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN deadline INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN dailyStatus TEXT DEFAULT '{}' NOT NULL")
            }
        }

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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