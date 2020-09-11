package com.krystofmacek.runtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * @HiltAndroidApp kicks off the code generation of the Hilt components and
 *  also generates a base class for your application that uses those generated components.
 * */
@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}