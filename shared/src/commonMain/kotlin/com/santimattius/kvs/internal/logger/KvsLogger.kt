package com.santimattius.kvs.internal.logger

internal interface KvsLogger {

    fun info(message: String)

    fun error(message: String, throwable: Throwable)
}

internal object NoopKvsLogger : KvsLogger {
    override fun info(message: String) = Unit
    override fun error(message: String, throwable: Throwable) = Unit
}