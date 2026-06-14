package com.teamsync.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "teamsync_session")

class SessionStore(private val ctx: Context) {
    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_USER_NAME = stringPreferencesKey("user_name")
    private val KEY_USER_EMAIL = stringPreferencesKey("user_email")

    @Volatile var token: String? = null
        private set

    private val _state = MutableStateFlow(Snapshot(null, null, null))
    val state: StateFlow<Snapshot> = _state

    data class Snapshot(val token: String?, val name: String?, val email: String?)

    init {
        runBlocking {
            val p = ctx.dataStore.data.first()
            token = p[KEY_TOKEN]
            _state.value = Snapshot(p[KEY_TOKEN], p[KEY_USER_NAME], p[KEY_USER_EMAIL])
        }
    }

    suspend fun save(token: String, name: String, email: String) {
        ctx.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_USER_NAME] = name
            it[KEY_USER_EMAIL] = email
        }
        this.token = token
        _state.value = Snapshot(token, name, email)
    }

    suspend fun clear() {
        ctx.dataStore.edit { it.clear() }
        token = null
        _state.value = Snapshot(null, null, null)
    }
}
