package com.teamsync.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamsync.app.data.Drill
import com.teamsync.app.data.Match
import com.teamsync.app.data.ServiceLocator
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUi(
    val loading: Boolean = true,
    val matches: List<Match> = emptyList(),
    val drill: Drill? = null,
    val drillLoaded: Boolean = false,
    val error: String? = null,
)

class DashboardViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val _ui = MutableStateFlow(DashboardUi())
    val ui: StateFlow<DashboardUi> = _ui

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)

        // Parallel fetches with per-call error reporting (so one failure doesn't
        // swallow the other).
        val matchesDeferred = async {
            runCatching { api.upcomingMatches() }
                .onFailure { Log.e("Dashboard", "upcomingMatches failed", it) }
                .getOrDefault(emptyList())
        }
        val drillDeferred = async {
            runCatching { api.dailyDrill() }
                .onFailure { Log.e("Dashboard", "dailyDrill failed", it) }
                .getOrNull()
        }

        val matches = matchesDeferred.await()
        val drill = drillDeferred.await()

        _ui.value = DashboardUi(
            loading = false,
            matches = matches,
            drill = drill,
            drillLoaded = true,
            error = if (matches.isEmpty() && drill == null) "Could not reach server." else null,
        )
    }

    fun rsvp(matchId: Int, status: String) = viewModelScope.launch {
        runCatching { api.rsvp(matchId, status) }
            .onFailure { Log.e("Dashboard", "rsvp failed", it) }
        _ui.value = _ui.value.copy(
            matches = _ui.value.matches.map { if (it.id == matchId) it.copy(my_rsvp = status) else it }
        )
    }
}
