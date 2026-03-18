package com.habitpulse.app.data.repository

import com.habitpulse.app.data.local.CompletionEntity
import com.habitpulse.app.data.local.HabitDao
import com.habitpulse.app.data.local.HabitEntity
import com.habitpulse.app.domain.model.Habit
import com.habitpulse.app.domain.model.HabitCompletion
import com.habitpulse.app.domain.model.HabitStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Repository that mediates between the data layer ([HabitDao]) and the UI layer.
 *
 * All public methods either return [Flow]-based reactive streams for observation
 * or are suspend functions for one-shot write operations. The repository handles
 * mapping between Room entities and domain models, as well as computing derived
 * data such as streaks and statistics.
 *
 * @property dao The [HabitDao] used to access the local Room database.
 */
class HabitRepository(private val dao: HabitDao) {

    /**
     * Observes all habits combined with today's completion status.
     *
     * Each emitted list contains [Habit] domain models with the [Habit.isCompletedToday]
     * field populated based on whether a completion record exists for today's date.
     *
     * @return A [Flow] of all habits with current-day completion status.
     */
    fun observeAllHabitsWithStatus(): Flow<List<Habit>> {
        val todayMillis = todayMidnightMillis()
        return combine(
            dao.observeAllHabits(),
            dao.observeCompletionsForDate(todayMillis)
        ) { habits, completions ->
            val completedIds = completions.map { it.habitId }.toSet()
            habits.map { entity ->
                entity.toDomain(isCompletedToday = entity.id in completedIds)
            }
        }
    }

    /**
     * Observes a single habit by ID with today's completion status.
     *
     * @param habitId The ID of the habit to observe.
     * @return A [Flow] emitting the [Habit] or null if it does not exist.
     */
    fun observeHabitWithStatus(habitId: Long): Flow<Habit?> {
        val todayMillis = todayMidnightMillis()
        return combine(
            dao.observeHabitById(habitId),
            dao.observeCompletionsForDate(todayMillis)
        ) { entity, completions ->
            entity?.toDomain(
                isCompletedToday = completions.any { it.habitId == habitId }
            )
        }
    }

    /**
     * Observes the completion history for a specific habit.
     *
     * @param habitId The ID of the habit whose completions to observe.
     * @return A [Flow] of [HabitCompletion] domain models ordered by date descending.
     */
    fun observeCompletions(habitId: Long): Flow<List<HabitCompletion>> {
        return dao.observeCompletionsForHabit(habitId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Creates a new habit in the database.
     *
     * @param habit The [Habit] domain model to persist. The [Habit.id] field is ignored
     *   as Room auto-generates the primary key.
     * @return The auto-generated ID of the newly created habit.
     */
    suspend fun createHabit(habit: Habit): Long {
        return dao.insertHabit(habit.toEntity())
    }

    /**
     * Updates an existing habit's properties.
     *
     * @param habit The [Habit] domain model with updated fields. The [Habit.id] must
     *   correspond to an existing record.
     */
    suspend fun updateHabit(habit: Habit) {
        dao.updateHabit(habit.toEntity())
    }

    /**
     * Permanently deletes a habit and all its completion records.
     *
     * @param habitId The ID of the habit to delete.
     */
    suspend fun deleteHabit(habitId: Long) {
        dao.deleteHabitById(habitId)
    }

    /**
     * Toggles the completion status of a habit for today.
     *
     * If the habit is not yet completed today, a new completion record is inserted.
     * If it is already completed, the existing record is removed.
     *
     * @param habitId The ID of the habit to toggle.
     * @param isCompleted The desired completion state: true to mark complete, false to unmark.
     */
    suspend fun toggleCompletion(habitId: Long, isCompleted: Boolean) {
        val todayMillis = todayMidnightMillis()
        if (isCompleted) {
            dao.insertCompletion(
                CompletionEntity(
                    habitId = habitId,
                    dateMillis = todayMillis
                )
            )
        } else {
            dao.deleteCompletion(habitId, todayMillis)
        }
    }

    /**
     * Computes comprehensive statistics for a given habit.
     *
     * This method performs several calculations:
     * 1. **Current streak**: Counts backward from today to find consecutive completed days.
     * 2. **Best streak**: Scans the full completion history to find the longest run.
     * 3. **Completion rate**: Ratio of completed days to total days since habit creation.
     * 4. **Weekly data**: Completion status for the last 7 days.
     * 5. **Monthly data**: Completion status for the last 30 days.
     *
     * @param habitId The ID of the habit to compute statistics for.
     * @return The computed [HabitStats], or null if the habit does not exist.
     */
    suspend fun getHabitStats(habitId: Long): HabitStats? {
        val habit = dao.getHabitById(habitId) ?: return null
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // Fetch all completions for this habit
        val allCompletions = dao.getCompletionsInRange(
            habitId = habitId,
            startDate = 0L,
            endDate = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        )
        val completedDates = allCompletions.map { millis ->
            Instant.ofEpochMilli(millis.dateMillis).atZone(zone).toLocalDate()
        }.toSet()

        // Current streak: count backward from today
        var currentStreak = 0
        var checkDate = today
        while (checkDate in completedDates) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Best streak: scan all completion dates in order
        val sortedDates = completedDates.sorted()
        var bestStreak = 0
        var runningStreak = 0
        var previousDate: LocalDate? = null
        for (date in sortedDates) {
            if (previousDate != null && ChronoUnit.DAYS.between(previousDate, date) == 1L) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > bestStreak) {
                bestStreak = runningStreak
            }
            previousDate = date
        }

        // Completion rate: days completed / days since creation
        val creationDate = Instant.ofEpochMilli(habit.createdAt).atZone(zone).toLocalDate()
        val totalDays = ChronoUnit.DAYS.between(creationDate, today).toInt() + 1
        val completionRate = if (totalDays > 0) {
            completedDates.size.toFloat() / totalDays.toFloat()
        } else {
            0f
        }

        // Weekly data (last 7 days)
        val weeklyData = (6 downTo 0).map { daysAgo ->
            today.minusDays(daysAgo.toLong()) in completedDates
        }

        // Monthly data (last 30 days)
        val monthlyData = (29 downTo 0).map { daysAgo ->
            today.minusDays(daysAgo.toLong()) in completedDates
        }

        return HabitStats(
            habitId = habitId,
            habitName = habit.name,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalCompletions = completedDates.size,
            completionRate = completionRate.coerceIn(0f, 1f),
            weeklyData = weeklyData,
            monthlyData = monthlyData
        )
    }

    /**
     * Retrieves all habits that have reminders configured (non-reactive).
     *
     * @return A list of [Habit] domain models with non-null [Habit.reminderTime].
     */
    suspend fun getHabitsWithReminders(): List<Habit> {
        return dao.getHabitsWithReminders().map { it.toDomain() }
    }

    /**
     * Retrieves all habits as a non-reactive snapshot (used by widgets).
     *
     * @return A list of all [Habit] domain models.
     */
    suspend fun getAllHabitsSnapshot(): List<Habit> {
        val todayMillis = todayMidnightMillis()
        val habits = dao.getAllHabits()
        val completions = dao.getCompletionsForDate(todayMillis)
        val completedIds = completions.map { it.habitId }.toSet()
        return habits.map { it.toDomain(isCompletedToday = it.id in completedIds) }
    }

    /**
     * Returns the epoch milliseconds for the start of today (midnight) in the device timezone.
     */
    private fun todayMidnightMillis(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    /**
     * Maps a [HabitEntity] to a [Habit] domain model.
     *
     * @param isCompletedToday Whether the habit has been completed today.
     * @return The mapped [Habit] domain model.
     */
    private fun HabitEntity.toDomain(isCompletedToday: Boolean = false): Habit {
        return Habit(
            id = id,
            name = name,
            description = description,
            frequency = frequency,
            reminderTime = reminderTime,
            createdAt = createdAt,
            isCompletedToday = isCompletedToday
        )
    }

    /**
     * Maps a [Habit] domain model to a [HabitEntity] for persistence.
     *
     * @return The mapped [HabitEntity].
     */
    private fun Habit.toEntity(): HabitEntity {
        return HabitEntity(
            id = id,
            name = name,
            description = description,
            frequency = frequency,
            reminderTime = reminderTime,
            createdAt = createdAt
        )
    }

    /**
     * Maps a [CompletionEntity] to a [HabitCompletion] domain model.
     *
     * @return The mapped [HabitCompletion] domain model.
     */
    private fun CompletionEntity.toDomain(): HabitCompletion {
        return HabitCompletion(
            id = id,
            habitId = habitId,
            dateMillis = dateMillis,
            completedAt = completedAt
        )
    }
}
