package com.teamsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamsync.app.data.ServiceLocator
import com.teamsync.app.data.SkillLevels
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUi(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val levels: SkillLevels = SkillLevels(),
    val saved: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val _ui = MutableStateFlow(ProfileUi())
    val ui: StateFlow<ProfileUi> = _ui

    init { load() }

    fun load() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        runCatching { api.getSkills() }
            .onSuccess { _ui.value = _ui.value.copy(loading = false, levels = it, saved = false) }
            .onFailure { _ui.value = _ui.value.copy(loading = false, error = it.message) }
    }

    fun update(transform: (SkillLevels) -> SkillLevels) {
        _ui.value = _ui.value.copy(levels = transform(_ui.value.levels), saved = false)
    }

    fun save() = viewModelScope.launch {
        _ui.value = _ui.value.copy(saving = true, saved = false)
        runCatching { api.saveSkills(_ui.value.levels) }
            .onSuccess { _ui.value = _ui.value.copy(saving = false, saved = true) }
            .onFailure { _ui.value = _ui.value.copy(saving = false, error = it.message) }
    }
}
