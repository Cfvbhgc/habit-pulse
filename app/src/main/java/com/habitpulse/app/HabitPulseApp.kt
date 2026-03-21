package com.habitpulse.app

import android.app.Application
import com.habitpulse.app.data.local.HabitDatabase
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.notification.NotificationHelper

/**
 * Custom [Application] class for HabitPulse.
 *
 * Serves as the composition root for the application's dependency graph.
 * Initialises the Room database, repository, and notification channel
 * as soon as the application process starts. These singletons are then
 * accessed by Activities, ViewModels, BroadcastReceivers, and widget providers.
 *
 * This class is registered in the AndroidManifest via `android:name=".HabitPulseApp"`.
 */
class HabitPulseApp : Application() {

    /**
     * The singleton Room database instance for the application.
     * Initialised lazily on first access to avoid blocking the main thread during startup.
     */
    val database: HabitDatabase by lazy {
        HabitDatabase.getInstance(this)
    }

    /**
     * The singleton repository that mediates between the database and the UI layer.
     * All ViewModels and background workers should use this instance for data access.
     */
    val repository: HabitRepository by lazy {
        HabitRepository(database.habitDao())
    }

    /**
     * Called when the application is first created.
     *
     * Initialises the notification channel so that reminders can be posted
     * at any time without requiring the channel to be created on demand.
     */
    override fun onCreate() {
        super.onCreate()
        // Create notification channel early so reminders work immediately
        NotificationHelper(this)
    }
}
