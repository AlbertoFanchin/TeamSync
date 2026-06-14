package com.teamsync.app.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val name: String? = null,
    val sport: String? = null,
)

@Serializable
data class AuthResponse(val token: String, val user: User)

@Serializable
data class User(
    val id: Int,
    val email: String,
    val name: String,
    val sport: String,
    val position: String? = null,
    val skill_gaps: List<String> = emptyList(),
)

@Serializable
data class Match(
    val id: Int,
    val opponent: String,
    val venue: String,
    val address: String? = null,
    val kickoff_at: String,
    val is_home: Int = 1,
    val my_rsvp: String? = null,
    val needs_ride: Int? = 0,
    val venue_lat: Double? = null,
    val venue_lng: Double? = null,
)

@Serializable
data class AttendanceEntry(
    val user_id: Int,
    val name: String,
    val position: String? = null,
    val status: String, // in | out | maybe | pending
    val needs_ride: Int = 0,
)

@Serializable
data class MatchDetail(val match: Match, val attendance: List<AttendanceEntry>)

@Serializable
data class CarpoolCreate(
    val match_id: Int,
    val seats: Int,
    val depart_at: String,
    val origin_lat: Double? = null,
    val origin_lng: Double? = null,
)

@Serializable
data class SkillLevels(
    val spiking: Float = 0f,
    val setting: Float = 0f,
    val digging: Float = 0f,
    val serving: Float = 0f,
    val blocking: Float = 0f,
    val passing: Float = 0f,
)

@Serializable
data class RsvpRequest(val status: String, val needs_ride: Int = 0)

@Serializable
data class Drill(
    val id: Int,
    val title: String,
    val skill_tag: String,
    val duration_s: Int = 0,
    val video_url: String = "",
    val thumbnail: String = "",
    val difficulty: Int = 1,
)

@Serializable
data class Passenger(
    val user_id: Int,
    val name: String,
    val pickup_lat: Double? = null,
    val pickup_lng: Double? = null,
    val stop_order: Int = 0,
)

@Serializable
data class RouteStop(
    val lat: Double,
    val lng: Double,
    val name: String? = null,
)

@Serializable
data class Carpool(
    val id: Int,
    val driver_id: Int,
    val driver_name: String,
    val seats: Int,
    val depart_at: String,
    val origin_lat: Double? = null,
    val origin_lng: Double? = null,
    val passengers: List<Passenger> = emptyList(),
    val route_json: List<RouteStop>? = null,
)
