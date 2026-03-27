package com.habitpulse.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.habitpulse.app.MainActivity
import com.habitpulse.app.R

/**
 * Helper class for creating and managing notification channels and notifications.
 *
 * Handles the creation of the habit reminder notification channel (required on Android 8+)
 * and provides a method to display individual habit reminder notifications.
 *
 * @property context The application context used for accessing system services and resources.
 */
class NotificationHelper(private val context: Context) {

    companion object {
        /** Unique ID for the habit reminders notification channel. */
        const val CHANNEL_ID = "habit_reminders"

        /** Base notification ID; each habit's notification uses this offset by the habit ID. */
        private const val NOTIFICATION_ID_BASE = 1000
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for habit reminders.
     *
     * This is safe to call multiple times; the system ignores the call if the channel
     * already exists. The channel is configured with default importance, which allows
     * the notification to appear in the status bar and make a sound.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Displays a reminder notification for a specific habit.
     *
     * The notification includes the habit name in the content text, uses the app icon,
     * and opens the main activity when tapped. Each habit receives a unique notification
     * ID based on its database ID, allowing multiple habit reminders to coexist in the
     * notification shade.
     *
     * @param habitId The database ID of the habit, used to generate a unique notification ID.
     * @param habitName The display name of the habit, shown in the notification body.
     */
    fun showReminderNotification(habitId: Long, habitName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text, habitName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(
            NOTIFICATION_ID_BASE + habitId.toInt(),
            notification
        )
    }
}
