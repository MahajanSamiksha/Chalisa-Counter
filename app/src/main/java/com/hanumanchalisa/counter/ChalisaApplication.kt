package com.hanumanchalisa.counter

import android.app.Application
import com.hanumanchalisa.counter.di.AppContainer

/**
 * Application entry point; owns the [AppContainer] so dependencies live as long as the process.
 */
class ChalisaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
