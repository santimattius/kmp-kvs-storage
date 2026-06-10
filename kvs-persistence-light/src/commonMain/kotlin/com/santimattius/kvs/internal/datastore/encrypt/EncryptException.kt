package com.santimattius.kvs.internal.datastore.encrypt

class EncryptException(message: String, cause: Throwable? = null) : Exception(message, cause)
class DecryptException(message: String, cause: Throwable? = null) : Exception(message, cause)
