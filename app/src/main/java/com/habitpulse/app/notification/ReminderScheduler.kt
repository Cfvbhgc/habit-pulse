package com.habitpulse.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.habitpulse.app.domain.model.Habit
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Utility class for scheduling and cancelling habit reminder alarms via [AlarmManager].
 *
 * Uses exact alarms ([AlarmManager.setExactAndAllowWhileIdle]) to ensure reminders
 * fire reliably even when the device is in Doze mode. Each habit receives a unique
 * [PendingIntent] keyed by its ID, so alarms can be individually cancelled or updated.
 *
 * Alarms are scheduled for the next occurrence of the reminder time. If the time has
 * already passed today, the alarm is scheduled for tomorrow.
 *
 * @property context The application context used for accessing [AlarmManager] and creating intents.
 */
class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a daily reminder alarm for a specific habit.
     *
     * The alarm triggers a broadcast to [ReminderReceiver], which in turn displays
     * the notification. If the specified time has already passed today, the alarm
     * is scheduled for the same time tomorrow.
     *
     * @param habit The [Habit] to schedule a reminder for. Must have a non-null
     *   [Habit.reminderTime] in "HH:mm" format.
     */
    fun scheduleReminder(habit: Habit) {
        val reminderTime = habit.reminderTime ?: return

        val timeParts = reminderTime.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val now = java.time.ZonedDateTime.now()
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val targetTime = LocalTime.of(hour, minute)
        var triggerDateTime = today.atTime(targetTime).atZone(zone)

        // If the time has already passed today, schedule for tomorrow
        if (triggerDateTime.isBefore(now) || triggerDateTime.isEqual(now)) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }

        val triggerMillis = triggerDateTime.toInstant().toEpochMilli()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderReceiver.EXTRA_HABIT_NAME, habit.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }

    /**
     * Cancels a previously scheduled reminder alarm for a specific habit.
     *
     * @param habitId The ID of the habit whose reminder should be cancelled.
     */
    fun cancelReminder(habitId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Schedules reminders for all habits that have a reminder time configured.
     *
     * This is typically called after a device reboot to restore all alarms, since
     * [AlarmManager] alarms do not persist across reboots.
     *
     * @param habits The list of habits to schedule reminders for. Only habits with
     *   non-null [Habit.reminderTime] will have alarms scheduled.
     */
    fun scheduleAllReminders(habits: List<Habit>) {
        habits.forEach { habit ->
            if (habit.reminderTime != null) {
                scheduleReminder(habit)
            }
        }
    }
}
