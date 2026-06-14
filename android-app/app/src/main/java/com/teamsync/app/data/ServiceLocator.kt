package com.teamsync.app.data

import android.content.Context

object ServiceLocator {
    lateinit var session: SessionStore
        private set
    lateinit var api: ApiClient
        private set

    fun init(ctx: Context) {
        session = SessionStore(ctx.applicationContext)
        api = ApiClient(session)
    }
}
