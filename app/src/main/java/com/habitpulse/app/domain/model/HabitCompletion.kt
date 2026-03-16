package com.habitpulse.app.domain.model

/**
 * Domain model representing a single completion event for a habit.
 *
 * Each instance records that a particular habit was completed on a specific date.
 * This model is used for displaying completion history and computing streaks.
 *
 * @property id Unique identifier for this completion record.
 * @property habitId The ID of the habit that was completed.
 * @property dateMillis The normalised date (midnight epoch millis in the device's timezone)
 *   on which the habit was completed.
 * @property completedAt The exact epoch milliseconds when the user tapped "complete".
 */
data class HabitCompletion(
    val id: Long = 0L,
    val habitId: Long,
    val dateMillis: Long,
    val completedAt: Long = System.currentTimeMillis()
)
