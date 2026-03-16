package com.habitpulse.app.domain.model

/**
 * Aggregated statistics for a single habit.
 *
 * This data class holds computed metrics used by the statistics and detail screens
 * to visualise a habit's performance over time.
 *
 * @property habitId The ID of the habit these statistics belong to.
 * @property habitName The display name of the habit, included for convenience in UI rendering.
 * @property currentStreak The number of consecutive days (up to and including today)
 *   that the habit has been completed without a gap.
 * @property bestStreak The longest consecutive completion streak in the habit's entire history.
 * @property totalCompletions The total number of times the habit has ever been marked complete.
 * @property completionRate A value between 0.0 and 1.0 representing the percentage of days
 *   the habit was completed since its creation date.
 * @property weeklyData A list of 7 boolean values representing the last 7 days (index 0 = 6 days ago,
 *   index 6 = today), where true means the habit was completed on that day.
 * @property monthlyData A list of up to 30 boolean values representing the last 30 days
 *   (index 0 = 29 days ago, index last = today), where true means completed.
 */
data class HabitStats(
    val habitId: Long,
    val habitName: String,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completionRate: Float,
    val weeklyData: List<Boolean>,
    val monthlyData: List<Boolean>
)
