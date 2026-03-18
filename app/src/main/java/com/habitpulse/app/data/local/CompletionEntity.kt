package com.habitpulse.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity that tracks individual habit completions.
 *
 * Each row represents a single day on which a habit was marked as completed.
 * A composite unique index on [habitId] and [dateMillis] prevents duplicate
 * completions for the same habit on the same day.
 *
 * A foreign key relationship to [HabitEntity] ensures referential integrity:
 * when a habit is deleted, all of its completion records are automatically removed.
 *
 * @property id Auto-generated unique identifier for this completion record.
 * @property habitId The ID of the habit that was completed, referencing [HabitEntity.id].
 * @property dateMillis Epoch milliseconds representing the start of the day (midnight)
 *   on which the habit was completed. Normalised to midnight for consistent date comparison.
 * @property completedAt Epoch milliseconds of the exact moment the user marked the habit complete.
 */
@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "dateMillis"], unique = true),
        Index(value = ["habitId"])
    ]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val dateMillis: Long,
    val completedAt: Long = System.currentTimeMillis()
)
