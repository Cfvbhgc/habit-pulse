package com.habitpulse.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitpulse.app.data.local.HabitDatabase
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.model.Habit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that handles fired habit reminder alarms.
 *
 * When an alarm triggers, this receiver:
 * 1. Displays a notification reminding the user to complete the habit.
 * 2. Reschedules the alarm for the next day (since [AlarmManager.setExactAndAllowWhileIdle]
 *    is a one-shot alarm and does not repeat automatically).
 *
 * The receiver uses a coroutine scope with [Dispatchers.IO] for database access
 * and [SupervisorJob] to prevent failures in one operation from cancelling others.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        /** Intent extra key for the habit's database ID. */
        const val EXTRA_HABIT_ID = "extra_habit_id"

        /** Intent extra key for the habit's display name. */
        const val EXTRA_HABIT_NAME = "extra_habit_name"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Called when the alarm fires. Shows the reminder notification and reschedules
     * the alarm for the next day.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent containing the habit ID and name as extras.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME) ?: return

        if (habitId == -1L) return

        // Show the notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showReminderNotification(habitId, habitName)

        // Reschedule for the next day
        val pendingResult = goAsync()
        scope.launch {
            try {
                val database = HabitDatabase.getInstance(context)
                val dao = database.habitDao()
                val habitEntity = dao.getHabitById(habitId)

                if (habitEntity != null && habitEntity.reminderTime != null) {
                    val habit = Habit(
                        id = habitEntity.id,
                        name = habitEntity.name,
                        description = habitEntity.description,
                        frequency = habitEntity.frequency,
                        reminderTime = habitEntity.reminderTime,
                        createdAt = habitEntity.createdAt
                    )
                    val scheduler = ReminderScheduler(context)
                    scheduler.scheduleReminder(habit)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
