package com.habitpulse.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.habitpulse.app.MainActivity
import com.habitpulse.app.R
import com.habitpulse.app.data.local.HabitDatabase
import com.habitpulse.app.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * AppWidgetProvider that displays today's habits progress on the home screen.
 *
 * The widget shows a summary of how many habits have been completed today
 * out of the total number of habits. Tapping the widget opens the main
 * activity so the user can quickly mark habits as complete.
 *
 * Updates are triggered by the system at the interval specified in the widget
 * info XML (every 30 minutes by default), as well as whenever the user adds
 * or removes the widget from their home screen.
 */
class HabitWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Called when the widget needs to be updated.
     *
     * Queries the database for today's habits and their completion status,
     * then updates each widget instance with a summary text.
     *
     * @param context The Context in which the receiver is running.
     * @param appWidgetManager The [AppWidgetManager] for updating widget views.
     * @param appWidgetIds The IDs of all widget instances that need updating.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            val database = HabitDatabase.getInstance(context)
            val repository = HabitRepository(database.habitDao())
            val habits = repository.getAllHabitsSnapshot()

            val completedCount = habits.count { it.isCompletedToday }
            val totalCount = habits.size

            val summaryText = if (totalCount == 0) {
                "No habits yet. Tap to get started!"
            } else {
                buildString {
                    append("$completedCount of $totalCount completed today\n\n")
                    habits.take(5).forEach { habit ->
                        val checkMark = if (habit.isCompletedToday) "[x]" else "[ ]"
                        append("$checkMark ${habit.name}\n")
                    }
                    if (habits.size > 5) {
                        append("...and ${habits.size - 5} more")
                    }
                }
            }

            for (widgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, widgetId, summaryText)
            }
        }
    }

    /**
     * Updates a single widget instance with the given summary text.
     *
     * Sets up the click handler to open the main activity and updates the
     * summary text view with the current habits status.
     *
     * @param context The application context.
     * @param appWidgetManager The widget manager for applying the update.
     * @param widgetId The specific widget instance ID to update.
     * @param summaryText The formatted summary text to display.
     */
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        summaryText: String
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_habit_list).apply {
            setTextViewText(R.id.widget_habits_summary, summaryText)
            setOnClickPendingIntent(R.id.widget_title, pendingIntent)
            setOnClickPendingIntent(R.id.widget_habits_summary, pendingIntent)
        }

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    /**
     * Called when the widget is removed from the home screen.
     * Currently no cleanup is required.
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
    }

    /**
     * Called when the first widget instance is placed on the home screen.
     * Currently no initial setup is required beyond the standard update.
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    /**
     * Called when the last widget instance is removed from the home screen.
     * Currently no cleanup is required.
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }
}
