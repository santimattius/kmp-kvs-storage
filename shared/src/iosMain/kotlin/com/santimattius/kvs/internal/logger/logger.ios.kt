package com.santimattius.kvs.internal.logger

import platform.Foundation.NSLog

internal actual fun logger(): KvsLogger {
    return IosKvsLogger()
}

private class IosKvsLogger : KvsLogger {
    override fun info(message: String) {
        NSLog(message)
    }

    override fun error(message: String, throwable: Throwable) {
        //TODO: print exception
        NSLog(message)
    }

}