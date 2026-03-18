package com.habitpulse.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database for the HabitPulse application.
 *
 * This database contains two tables:
 * - **habits**: Stores habit definitions (name, description, frequency, reminder time).
 * - **completions**: Tracks daily completion records for each habit.
 *
 * The database is created as a singleton via [getInstance] to prevent multiple
 * instances from opening simultaneous connections. Migration support is built in
 * to handle schema evolution across app updates.
 */
@Database(
    entities = [HabitEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HabitDatabase : RoomDatabase() {

    /**
     * Provides access to the [HabitDao] for performing habit and completion operations.
     *
     * @return The DAO instance bound to this database.
     */
    abstract fun habitDao(): HabitDao

    companion object {

        private const val DATABASE_NAME = "habit_pulse_db"

        @Volatile
        private var instance: HabitDatabase? = null

        /**
         * Returns the singleton [HabitDatabase] instance, creating it if necessary.
         *
         * This method is thread-safe. The database is built with:
         * - All defined migrations for forward-compatible schema updates.
         * - Fallback to destructive migration as a last resort if a migration path is missing.
         *
         * @param context Application context used to build the database.
         * @return The singleton database instance.
         */
        fun getInstance(context: Context): HabitDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*allMigrations)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }

        /**
         * Placeholder migration from version 1 to 2.
         *
         * This migration is defined proactively to demonstrate the migration pattern.
         * When a schema change is needed, the SQL statements should be added here
         * and the database version bumped accordingly.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Future migration: e.g., adding a "color" column to habits
                // db.execSQL("ALTER TABLE habits ADD COLUMN color TEXT NOT NULL DEFAULT '#FF6750A4'")
            }
        }

        /**
         * Array of all migrations to be applied to the database builder.
         * Add new migrations here as the schema evolves.
         */
        private val allMigrations: Array<Migration> = arrayOf(
            // MIGRATION_1_2  // Uncomment when version is bumped to 2
        )
    }
}
