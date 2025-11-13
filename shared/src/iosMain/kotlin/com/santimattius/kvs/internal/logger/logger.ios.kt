package com.santimattius.kvs.internal.logger

import platform.Foundation.NSLog

/**
 * Returns an iOS-specific logger.
 * @return A [KvsLogger] instance that logs to NSLog.
 */
internal actual fun logger(): KvsLogger {
    return IosKvsLogger()
}

/**
 * A [KvsLogger] implementation that uses NSLog for logging on iOS.
 */
private class IosKvsLogger : KvsLogger {
    override fun info(message: String) {
        NSLog(message)
    }

    override fun error(message: String, throwable: Throwable) {
        //TODO: print exception
        NSLog(message)
    }

}