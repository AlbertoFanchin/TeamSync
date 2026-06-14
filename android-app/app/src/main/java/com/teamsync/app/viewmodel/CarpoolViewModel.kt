package com.teamsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamsync.app.data.Carpool
import com.teamsync.app.data.CarpoolCreate
import com.teamsync.app.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CarpoolUi(
    val loading: Boolean = true,
    val cars: List<Carpool> = emptyList(),
    val optimizing: Boolean = false,
    val offering: Boolean = false,
    val offerError: String? = null,
    val matchId: Int = 3, // first upcoming match in the seed
)

class CarpoolViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val _ui = MutableStateFlow(CarpoolUi())
    val ui: StateFlow<CarpoolUi> = _ui

    init { load() }

    fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true)
        val cars = runCatching { api.carpools(_ui.value.matchId) }.getOrDefault(emptyList())
        _ui.value = _ui.value.copy(loading = false, cars = cars)
    }

    fun optimize() = viewModelScope.launch {
        _ui.value = _ui.value.copy(optimizing = true)
        runCatching { api.optimizeCarpools(_ui.value.matchId) }
        val cars = runCatching { api.carpools(_ui.value.matchId) }.getOrDefault(emptyList())
        _ui.value = _ui.value.copy(optimizing = false, cars = cars)
    }

    fun offerRide(seats: Int, departAt: String, onDone: () -> Unit = {}) = viewModelScope.launch {
        _ui.value = _ui.value.copy(offering = true, offerError = null)
        runCatching {
            api.createCarpool(CarpoolCreate(
                match_id = _ui.value.matchId,
                seats = seats,
                depart_at = departAt,
            ))
        }
            .onSuccess {
                val cars = runCatching { api.carpools(_ui.value.matchId) }.getOrDefault(emptyList())
                _ui.value = _ui.value.copy(offering = false, cars = cars)
                onDone()
            }
            .onFailure {
                _ui.value = _ui.value.copy(offering = false, offerError = it.message ?: "offer_failed")
            }
    }
}
