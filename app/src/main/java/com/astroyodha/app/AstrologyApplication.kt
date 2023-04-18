package com.astroyodha.app

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.astroyodha.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AstrologyApplication : Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
//        if (BuildConfig.DEBUG) {
//            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
//        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        // perform common functionality that you want to perform on stop
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // perform common functionality that you want to perform on stop
    }

}