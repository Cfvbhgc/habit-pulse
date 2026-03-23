package com.habitpulse.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.model.Habit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen that manages the list of today's habits.
 *
 * Observes the habit repository for changes and exposes a [StateFlow] of [HomeUiState]
 * that the composable UI collects to render the current state. Provides actions
 * for toggling habit completion status.
 *
 * @property repository The [HabitRepository] used to access and modify habit data.
 */
class HomeViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    /**
     * The current UI state for the home screen.
     *
     * Emits [HomeUiState.Loading] initially, then transitions to [HomeUiState.Success]
     * containing the list of habits with their current-day completion status.
     * The flow is shared eagerly and retains the latest value for 5 seconds after
     * the last subscriber disconnects, preventing unnecessary re-queries during
     * configuration changes.
     */
    val uiState: StateFlow<HomeUiState> = repository.observeAllHabitsWithStatus()
        .map { habits -> HomeUiState.Success(habits = habits) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    /**
     * Toggles the completion status of a habit for the current day.
     *
     * If [isCompleted] is true, a completion record is created for today.
     * If false, the existing completion record is removed.
     *
     * @param habitId The ID of the habit to toggle.
     * @param isCompleted The desired completion state.
     */
    fun toggleCompletion(habitId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, isCompleted)
        }
    }
}

/**
 * Represents the possible UI states for the home screen.
 */
sealed interface HomeUiState {

    /**
     * Initial loading state before data is available.
     */
    data object Loading : HomeUiState

    /**
     * Data loaded successfully.
     *
     * @property habits The list of all habits with their current-day completion status.
     */
    data class Success(val habits: List<Habit>) : HomeUiState
}
