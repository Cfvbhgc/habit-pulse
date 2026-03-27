package com.habitpulse.app.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver that triggers a widget update when habit data changes.
 *
 * This receiver can be sent a broadcast from anywhere in the app (e.g., after
 * a habit is completed or created) to force all widget instances to refresh
 * their displayed data immediately, rather than waiting for the next scheduled
 * update interval.
 *
 * Usage from within the app:
 * ```kotlin
 * val intent = Intent(context, HabitWidgetReceiver::class.java)
 * context.sendBroadcast(intent)
 * ```
 */
class HabitWidgetReceiver : BroadcastReceiver() {

    /**
     * Triggers an update of all [HabitWidgetProvider] widget instances.
     *
     * Retrieves all active widget IDs and sends an [AppWidgetManager.ACTION_APPWIDGET_UPDATE]
     * broadcast to the widget provider, causing [HabitWidgetProvider.onUpdate] to be called.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The received intent (contents are ignored; only the broadcast itself matters).
     */
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, HabitWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

        if (widgetIds.isNotEmpty()) {
            val updateIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            }
            context.sendBroadcast(updateIntent)
        }
    }
}
