package com.santimattius.kvs.internal.logger

import android.util.Log

/**
 * Returns an Android-specific logger.
 * @return A [KvsLogger] instance that logs to Logcat.
 */
internal actual fun logger(): KvsLogger {
    return AndroidKvsLogger()
}

/**
 * A [KvsLogger] implementation that uses Logcat for logging on Android.
 */
private class AndroidKvsLogger : KvsLogger {
    override fun info(message: String) {
        Log.i(TAG, message)
    }

    override fun error(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
}