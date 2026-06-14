package com.teamsync.app

import android.app.Application
import com.teamsync.app.data.ServiceLocator

class TeamSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
