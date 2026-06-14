package com.teamsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamsync.app.data.MatchDetail
import com.teamsync.app.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EventDetailUi(
    val loading: Boolean = true,
    val detail: MatchDetail? = null,
    val error: String? = null,
)

class EventDetailViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val _ui = MutableStateFlow(EventDetailUi())
    val ui: StateFlow<EventDetailUi> = _ui

    fun load(matchId: Int) = viewModelScope.launch {
        _ui.value = EventDetailUi(loading = true)
        runCatching { api.matchDetail(matchId) }
            .onSuccess { _ui.value = EventDetailUi(loading = false, detail = it) }
            .onFailure { _ui.value = EventDetailUi(loading = false, error = it.message) }
    }

    fun rsvp(matchId: Int, status: String) = viewModelScope.launch {
        runCatching { api.rsvp(matchId, status) }
        load(matchId)
    }
}
