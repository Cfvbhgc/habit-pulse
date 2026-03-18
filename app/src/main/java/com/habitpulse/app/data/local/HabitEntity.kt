package com.habitpulse.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a habit stored in the local database.
 *
 * Each habit has a unique identifier, a user-defined name and optional description,
 * a frequency indicating how often it should be performed, an optional reminder time
 * for scheduling notifications, and a creation timestamp for ordering and statistics.
 *
 * @property id Auto-generated unique identifier for the habit.
 * @property name The display name of the habit (e.g., "Drink Water").
 * @property description An optional longer description providing context for the habit.
 * @property frequency How often the habit should be completed: "daily", "weekly", or "monthly".
 * @property reminderTime Optional time string in "HH:mm" format for scheduling reminders.
 * @property createdAt Epoch milliseconds when the habit was first created.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val frequency: String = "daily",
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
