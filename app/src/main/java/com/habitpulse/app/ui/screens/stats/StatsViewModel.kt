package com.habitpulse.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.model.HabitStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the statistics screen that displays detailed habit performance data.
 *
 * Loads comprehensive statistics for a specific habit including streak counts,
 * completion rates, weekly bar chart data, and monthly calendar heatmap data.
 *
 * @property repository The [HabitRepository] used to compute statistics.
 * @property habitId The ID of the habit to display statistics for.
 */
class StatsViewModel(
    private val repository: HabitRepository,
    private val habitId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)

    /**
     * The current UI state for the statistics screen.
     *
     * Transitions from [StatsUiState.Loading] to [StatsUiState.Success] with computed
     * statistics, or to [StatsUiState.Error] if the habit cannot be found.
     */
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    /**
     * Loads statistics for the habit from the repository.
     *
     * Computes current streak, best streak, completion rate, and daily completion
     * data for both weekly and monthly views.
     */
    private fun loadStats() {
        viewModelScope.launch {
            val stats = repository.getHabitStats(habitId)
            _uiState.value = if (stats != null) {
                StatsUiState.Success(stats = stats)
            } else {
                StatsUiState.Error(message = "Habit not found")
            }
        }
    }

    /**
     * Refreshes the statistics data. Called when the user returns to this screen
     * or performs a pull-to-refresh gesture.
     */
    fun refresh() {
        loadStats()
    }
}

/**
 * Represents the possible UI states for the statistics screen.
 */
sealed interface StatsUiState {

    /**
     * Initial loading state while statistics are being computed.
     */
    data object Loading : StatsUiState

    /**
     * Statistics loaded successfully.
     *
     * @property stats The computed [HabitStats] containing all statistical data.
     */
    data class Success(val stats: HabitStats) : StatsUiState

    /**
     * An error occurred while loading statistics.
     *
     * @property message A human-readable error description.
     */
    data class Error(val message: String) : StatsUiState
}
