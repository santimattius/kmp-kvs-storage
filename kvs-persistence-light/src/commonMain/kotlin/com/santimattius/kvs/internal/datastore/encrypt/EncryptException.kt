package com.santimattius.kvs.internal.datastore.encrypt

internal class EncryptException(message: String, cause: Throwable? = null) : Exception(message, cause)
internal class DecryptException(message: String, cause: Throwable? = null) : Exception(message, cause)
