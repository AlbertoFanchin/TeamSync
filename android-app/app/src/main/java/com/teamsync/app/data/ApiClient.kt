package com.teamsync.app.data

import com.teamsync.app.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient(private val session: SessionStore) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val http = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            val t = session.token
            if (!t.isNullOrBlank()) bearerAuth(t)
        }
        expectSuccess = false
    }

    private fun url(path: String) = "${BuildConfig.API_BASE_URL}$path"

    suspend fun register(email: String, password: String, name: String, sport: String): AuthResponse {
        val res = http.post(url("/api/auth/register")) {
            setBody(AuthRequest(email, password, name, sport))
        }
        if (!res.status.isSuccess()) error("register_failed_${res.status.value}")
        return res.body()
    }

    suspend fun login(email: String, password: String): AuthResponse {
        val res = http.post(url("/api/auth/login")) {
            setBody(AuthRequest(email, password))
        }
        if (!res.status.isSuccess()) error("invalid_credentials")
        return res.body()
    }

    suspend fun upcomingMatches(): List<Match> = http.get(url("/api/matches/upcoming")).body()

    suspend fun matchDetail(matchId: Int): MatchDetail = http.get(url("/api/matches/$matchId")).body()

    suspend fun rsvp(matchId: Int, status: String, needsRide: Boolean = false) {
        http.post(url("/api/matches/$matchId/rsvp")) {
            setBody(RsvpRequest(status, if (needsRide) 1 else 0))
        }
    }

    suspend fun dailyDrill(): Drill = http.get(url("/api/drills/today")).body()

    suspend fun carpools(matchId: Int): List<Carpool> = http.get(url("/api/carpools/$matchId")).body()

    suspend fun createCarpool(c: CarpoolCreate) {
        http.post(url("/api/carpools")) { setBody(c) }
    }

    suspend fun optimizeCarpools(matchId: Int) {
        http.post(url("/api/carpools/$matchId/optimize"))
    }

    suspend fun getSkills(): SkillLevels = http.get(url("/api/me/skills")).body()

    suspend fun saveSkills(levels: SkillLevels) {
        http.post(url("/api/me/skills")) { setBody(levels) }
    }
}
