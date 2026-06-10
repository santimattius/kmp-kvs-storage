package com.santimattius.kvs.internal.exception

abstract class KvsException(message: String, cause: Throwable? = null) : Throwable(message, cause)

class ReadKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

class WriteKvsException(message: String, cause: Throwable? = null) : KvsException(message, cause)

class RemoveKvsException(message: String) : KvsException(message)

class ClearKvsException(message: String) : KvsException(message)

class ContainsKvsException(message: String) : KvsException(message)

class GetAllKvsException(message: String) : KvsException(message)
