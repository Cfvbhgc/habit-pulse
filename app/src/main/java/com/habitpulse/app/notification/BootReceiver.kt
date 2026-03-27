package com.habitpulse.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitpulse.app.data.local.HabitDatabase
import com.habitpulse.app.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that restores all habit reminder alarms after a device reboot.
 *
 * Android's [android.app.AlarmManager] does not persist alarms across reboots.
 * This receiver listens for [Intent.ACTION_BOOT_COMPLETED] and reschedules all
 * habit reminders by reading habits with configured reminder times from the database
 * and passing them to [ReminderScheduler].
 */
class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Called when the device finishes booting. Reschedules all habit reminders.
     *
     * Uses [goAsync] to extend the receiver's lifecycle while performing the
     * asynchronous database query and alarm scheduling.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The [Intent.ACTION_BOOT_COMPLETED] intent.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val database = HabitDatabase.getInstance(context)
                val repository = HabitRepository(database.habitDao())
                val habitsWithReminders = repository.getHabitsWithReminders()

                val scheduler = ReminderScheduler(context)
                scheduler.scheduleAllReminders(habitsWithReminders)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
