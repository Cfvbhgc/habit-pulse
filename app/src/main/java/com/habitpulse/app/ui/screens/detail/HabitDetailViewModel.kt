package com.habitpulse.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.model.Habit
import com.habitpulse.app.domain.model.HabitCompletion
import com.habitpulse.app.domain.model.HabitStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the habit detail screen that displays streak information and completion history.
 *
 * Loads a specific habit's data, statistics, and recent completion history, exposing
 * them as a single [StateFlow] of [HabitDetailUiState]. Provides actions for
 * toggling today's completion and deleting the habit.
 *
 * @property repository The [HabitRepository] used to access habit data.
 * @property habitId The ID of the habit to display.
 */
class HabitDetailViewModel(
    private val repository: HabitRepository,
    private val habitId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<HabitDetailUiState>(HabitDetailUiState.Loading)

    /**
     * The current UI state for the detail screen.
     *
     * Transitions from [HabitDetailUiState.Loading] to [HabitDetailUiState.Success]
     * once data is loaded, or to [HabitDetailUiState.Deleted] after deletion.
     */
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    init {
        observeHabit()
        loadStats()
    }

    /**
     * Starts observing the habit and its completion history from the repository.
     * Updates the UI state whenever the underlying data changes.
     */
    private fun observeHabit() {
        viewModelScope.launch {
            repository.observeHabitWithStatus(habitId).collect { habit ->
                if (habit != null) {
                    _uiState.update { current ->
                        when (current) {
                            is HabitDetailUiState.Success -> current.copy(habit = habit)
                            else -> HabitDetailUiState.Success(
                                habit = habit,
                                stats = null,
                                recentCompletions = emptyList()
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.observeCompletions(habitId).collect { completions ->
                _uiState.update { current ->
                    if (current is HabitDetailUiState.Success) {
                        current.copy(recentCompletions = completions.take(30))
                    } else {
                        current
                    }
                }
            }
        }
    }

    /**
     * Loads computed statistics for the habit (streaks, completion rate, chart data).
     */
    private fun loadStats() {
        viewModelScope.launch {
            val stats = repository.getHabitStats(habitId)
            _uiState.update { current ->
                if (current is HabitDetailUiState.Success) {
                    current.copy(stats = stats)
                } else {
                    HabitDetailUiState.Success(
                        habit = null,
                        stats = stats,
                        recentCompletions = emptyList()
                    )
                }
            }
        }
    }

    /**
     * Toggles the completion status of this habit for the current day
     * and refreshes the statistics.
     *
     * @param isCompleted The desired completion state.
     */
    fun toggleCompletion(isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, isCompleted)
            // Refresh stats after toggling
            val stats = repository.getHabitStats(habitId)
            _uiState.update { current ->
                if (current is HabitDetailUiState.Success) {
                    current.copy(stats = stats)
                } else {
                    current
                }
            }
        }
    }

    /**
     * Permanently deletes this habit and all its completion records.
     * Sets the UI state to [HabitDetailUiState.Deleted] to signal navigation.
     */
    fun deleteHabit() {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            _uiState.value = HabitDetailUiState.Deleted
        }
    }
}

/**
 * Represents the possible UI states for the habit detail screen.
 */
sealed interface HabitDetailUiState {

    /**
     * Initial loading state before data is available.
     */
    data object Loading : HabitDetailUiState

    /**
     * Data loaded successfully.
     *
     * @property habit The habit domain model with today's completion status, or null if loading.
     * @property stats Computed statistics for the habit, or null if still calculating.
     * @property recentCompletions The most recent completion records (up to 30).
     */
    data class Success(
        val habit: Habit?,
        val stats: HabitStats?,
        val recentCompletions: List<HabitCompletion>
    ) : HabitDetailUiState

    /**
     * The habit was deleted, signalling the UI to navigate back.
     */
    data object Deleted : HabitDetailUiState
}
