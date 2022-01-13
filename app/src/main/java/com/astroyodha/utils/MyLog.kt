package com.astroyodha.utils

import com.astroyodha.BuildConfig

/**
 * MyLog: Class to manage logs
 * it will only show log on debug build
 */
object MyLog {

    val LOG_ON = BuildConfig.DEBUG//true    //set true to show log on release build

    @kotlin.jvm.JvmStatic
    fun e(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.e(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun i(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.i(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun d(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.d(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun v(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.v(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun w(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.w(TAG, message)
    }

    @kotlin.jvm.JvmStatic
    fun wtf(TAG: String, message: String) {
        if (LOG_ON)
            android.util.Log.wtf(TAG, message)
    }
}
