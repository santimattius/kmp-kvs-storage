package com.santimattius.kvs.internal.logger

import android.util.Log

internal actual fun logger(): KvsLogger {
    return AndroidKvsLogger()
}

private class AndroidKvsLogger : KvsLogger {
    override fun info(message: String) {
        Log.i(TAG, message)
    }

    override fun error(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }
}