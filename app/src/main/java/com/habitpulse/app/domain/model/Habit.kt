package com.habitpulse.app.domain.model

/**
 * Domain model representing a user-defined habit.
 *
 * This is the primary model used throughout the UI and business logic layers.
 * It is mapped from [com.habitpulse.app.data.local.HabitEntity] by the repository
 * to decouple the presentation layer from the persistence implementation.
 *
 * @property id Unique identifier for the habit. A value of 0 indicates a new, unsaved habit.
 * @property name The user-facing name of the habit (e.g., "Exercise", "Read 30 minutes").
 * @property description An optional detailed description providing context or motivation.
 * @property frequency How often the habit should be performed: "daily", "weekly", or "monthly".
 * @property reminderTime Optional reminder time in "HH:mm" format (24-hour clock).
 *   When set, the app schedules a daily notification at this time.
 * @property createdAt Epoch milliseconds when the habit was first created.
 * @property isCompletedToday Whether the habit has been marked complete for the current day.
 *   This field is populated at the UI layer and defaults to false.
 */
data class Habit(
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val frequency: String = "daily",
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompletedToday: Boolean = false
)
