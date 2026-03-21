package com.habitpulse.app.ui.navigation

/**
 * Sealed class representing all navigation destinations in the HabitPulse application.
 *
 * Each object or class defines a unique route string used by the Navigation Compose
 * framework. Destinations that require arguments encode them as path parameters
 * in their route template.
 *
 * @property route The navigation route string. May contain `{paramName}` placeholders
 *   for destinations that accept arguments.
 */
sealed class Screen(val route: String) {

    /**
     * Home screen displaying today's habits with completion toggles.
     */
    data object Home : Screen("home")

    /**
     * Screen for creating a new habit. Uses the same composable as [EditHabit]
     * but without a pre-populated habit.
     */
    data object AddHabit : Screen("add_habit")

    /**
     * Screen for editing an existing habit.
     *
     * @see createRoute for building the navigation route with a specific habit ID.
     */
    data object EditHabit : Screen("edit_habit/{habitId}") {
        /**
         * Creates a concrete route for navigating to the edit screen for a specific habit.
         *
         * @param habitId The ID of the habit to edit.
         * @return The route string with the habit ID substituted.
         */
        fun createRoute(habitId: Long): String = "edit_habit/$habitId"
    }

    /**
     * Detail screen showing a habit's streak information and completion history.
     *
     * @see createRoute for building the navigation route with a specific habit ID.
     */
    data object HabitDetail : Screen("habit_detail/{habitId}") {
        /**
         * Creates a concrete route for navigating to the detail screen for a specific habit.
         *
         * @param habitId The ID of the habit to view.
         * @return The route string with the habit ID substituted.
         */
        fun createRoute(habitId: Long): String = "habit_detail/$habitId"
    }

    /**
     * Statistics screen showing weekly and monthly completion data.
     *
     * @see createRoute for building the navigation route with a specific habit ID.
     */
    data object Stats : Screen("stats/{habitId}") {
        /**
         * Creates a concrete route for navigating to the stats screen for a specific habit.
         *
         * @param habitId The ID of the habit to view statistics for.
         * @return The route string with the habit ID substituted.
         */
        fun createRoute(habitId: Long): String = "stats/$habitId"
    }
}
