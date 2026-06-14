package com.teamsync.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teamsync.app.data.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUi(
    val loading: Boolean = false,
    val error: String? = null,
    val authed: Boolean = false,
)

class AuthViewModel : ViewModel() {
    private val api = ServiceLocator.api
    private val session = ServiceLocator.session

    private val _ui = MutableStateFlow(AuthUi(authed = session.token != null))
    val ui: StateFlow<AuthUi> = _ui

    fun login(email: String, password: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        runCatching { api.login(email.trim(), password) }
            .onSuccess {
                session.save(it.token, it.user.name, it.user.email)
                _ui.value = AuthUi(authed = true)
            }
            .onFailure { _ui.value = AuthUi(error = it.message ?: "login_failed") }
    }

    fun register(email: String, password: String, name: String, sport: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        runCatching { api.register(email.trim(), password, name.trim(), sport) }
            .onSuccess {
                session.save(it.token, it.user.name, it.user.email)
                _ui.value = AuthUi(authed = true)
            }
            .onFailure { _ui.value = AuthUi(error = it.message ?: "register_failed") }
    }

    fun logout() = viewModelScope.launch {
        session.clear()
        _ui.value = AuthUi(authed = false)
    }
}
