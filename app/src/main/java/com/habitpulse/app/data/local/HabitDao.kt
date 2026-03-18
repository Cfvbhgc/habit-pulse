package com.habitpulse.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for habit-related database operations.
 *
 * Provides reactive [Flow]-based queries for observing habit and completion data,
 * as well as suspend functions for write operations. All queries are designed to
 * support the MVVM architecture by exposing observable streams that automatically
 * update the UI when underlying data changes.
 */
@Dao
interface HabitDao {

    /**
     * Observes all habits ordered by creation date (newest first).
     *
     * @return A [Flow] emitting the full list of [HabitEntity] whenever the habits table changes.
     */
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAllHabits(): Flow<List<HabitEntity>>

    /**
     * Retrieves a single habit by its unique identifier.
     *
     * @param habitId The ID of the habit to retrieve.
     * @return The matching [HabitEntity], or null if no habit exists with that ID.
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): HabitEntity?

    /**
     * Observes a single habit by its unique identifier as a reactive stream.
     *
     * @param habitId The ID of the habit to observe.
     * @return A [Flow] emitting the [HabitEntity] whenever it changes, or null if deleted.
     */
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun observeHabitById(habitId: Long): Flow<HabitEntity?>

    /**
     * Inserts a new habit into the database.
     *
     * @param habit The [HabitEntity] to insert.
     * @return The auto-generated row ID for the newly inserted habit.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    /**
     * Updates an existing habit in the database.
     *
     * @param habit The [HabitEntity] with updated fields. The [HabitEntity.id] must match an existing row.
     */
    @Update
    suspend fun updateHabit(habit: HabitEntity)

    /**
     * Deletes a habit from the database. Associated completions are cascade-deleted.
     *
     * @param habit The [HabitEntity] to delete.
     */
    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    /**
     * Deletes a habit by its ID. Associated completions are cascade-deleted via foreign key.
     *
     * @param habitId The ID of the habit to delete.
     */
    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    /**
     * Inserts a completion record for a specific habit and date.
     *
     * Uses [OnConflictStrategy.IGNORE] so that marking a habit complete twice on the
     * same day is a safe no-op.
     *
     * @param completion The [CompletionEntity] to insert.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: CompletionEntity)

    /**
     * Removes the completion record for a specific habit on a specific date,
     * effectively "unmarking" the habit as complete for that day.
     *
     * @param habitId The ID of the habit.
     * @param dateMillis The normalised date (midnight epoch millis) to uncomplete.
     */
    @Query("DELETE FROM completions WHERE habitId = :habitId AND dateMillis = :dateMillis")
    suspend fun deleteCompletion(habitId: Long, dateMillis: Long)

    /**
     * Observes all completions for a given habit, ordered by date descending.
     *
     * @param habitId The ID of the habit whose completions to observe.
     * @return A [Flow] emitting the list of [CompletionEntity] whenever completions change.
     */
    @Query("SELECT * FROM completions WHERE habitId = :habitId ORDER BY dateMillis DESC")
    fun observeCompletionsForHabit(habitId: Long): Flow<List<CompletionEntity>>

    /**
     * Observes all completions across all habits for a specific date.
     *
     * This is used by the home screen to determine which habits have been completed today.
     *
     * @param dateMillis The normalised date (midnight epoch millis) to query.
     * @return A [Flow] emitting completion records for the given date.
     */
    @Query("SELECT * FROM completions WHERE dateMillis = :dateMillis")
    fun observeCompletionsForDate(dateMillis: Long): Flow<List<CompletionEntity>>

    /**
     * Retrieves all completions for a habit within a date range (inclusive).
     *
     * Used for computing weekly and monthly statistics.
     *
     * @param habitId The ID of the habit.
     * @param startDate The start of the range (midnight epoch millis, inclusive).
     * @param endDate The end of the range (midnight epoch millis, inclusive).
     * @return List of [CompletionEntity] within the specified range.
     */
    @Query("SELECT * FROM completions WHERE habitId = :habitId AND dateMillis BETWEEN :startDate AND :endDate ORDER BY dateMillis ASC")
    suspend fun getCompletionsInRange(habitId: Long, startDate: Long, endDate: Long): List<CompletionEntity>

    /**
     * Counts the total number of completions for a given habit.
     *
     * @param habitId The ID of the habit.
     * @return The total completion count.
     */
    @Query("SELECT COUNT(*) FROM completions WHERE habitId = :habitId")
    suspend fun getCompletionCount(habitId: Long): Int

    /**
     * Retrieves all habits that have a reminder time configured.
     *
     * Used by [com.habitpulse.app.notification.ReminderScheduler] to schedule alarms
     * after boot or when reminders need to be refreshed.
     *
     * @return List of habits with non-null reminder times.
     */
    @Query("SELECT * FROM habits WHERE reminderTime IS NOT NULL")
    suspend fun getHabitsWithReminders(): List<HabitEntity>

    /**
     * Retrieves all habits as a non-reactive list. Used by the widget provider
     * which cannot observe flows.
     *
     * @return All habits ordered by creation date.
     */
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    suspend fun getAllHabits(): List<HabitEntity>

    /**
     * Retrieves all completions for a specific date as a non-reactive list.
     * Used by the widget provider.
     *
     * @param dateMillis The normalised date (midnight epoch millis).
     * @return List of completions for that date.
     */
    @Query("SELECT * FROM completions WHERE dateMillis = :dateMillis")
    suspend fun getCompletionsForDate(dateMillis: Long): List<CompletionEntity>
}
