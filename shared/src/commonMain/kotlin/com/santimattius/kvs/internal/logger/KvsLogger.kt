package com.santimattius.kvs.internal.logger

/**
 * A logger for KVS operations.
 */
internal interface KvsLogger {

    /**
     * Logs an info message.
     * @param message The message to log.
     */
    fun info(message: String)

    /**
     * Logs an error message.
     * @param message The message to log.
     * @param throwable The throwable to log.
     */
    fun error(message: String, throwable: Throwable)
}

/**
 * A no-op implementation of [KvsLogger].
 */
internal object NoopKvsLogger : KvsLogger {
    override fun info(message: String) = Unit
    override fun error(message: String, throwable: Throwable) = Unit
}