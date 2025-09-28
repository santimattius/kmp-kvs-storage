package com.santimattius.kvs.internal.logger

internal interface KvsLogger {

    fun info(message: String)

    fun error(message: String, throwable: Throwable)
}