package com.teamsync.app.nav

object Routes {
    const val WELCOME       = "welcome"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val DASHBOARD     = "dashboard"
    const val CARPOOL       = "carpool"
    const val TACTICS       = "tactics"
    const val PROFILE       = "profile"

    const val EVENT_DETAIL_BASE = "event"
    const val EVENT_DETAIL      = "$EVENT_DETAIL_BASE/{matchId}"
    fun eventDetail(matchId: Int) = "$EVENT_DETAIL_BASE/$matchId"
}
