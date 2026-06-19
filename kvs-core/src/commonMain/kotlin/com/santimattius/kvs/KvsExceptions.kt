package com.santimattius.kvs

abstract class KvsException(message: String, cause: Throwable? = null) : Throwable(message, cause)

class ReadKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

class WriteKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

class GetAllKvsException(message: String) : KvsException(message)
