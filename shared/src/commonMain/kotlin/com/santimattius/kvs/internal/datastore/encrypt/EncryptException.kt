package com.santimattius.kvs.internal.datastore.encrypt

import com.santimattius.kvs.internal.exception.KvsException

class EncryptException(message: String, cause: Throwable? = null) : KvsException(message, cause)
class DecryptException(message: String) : KvsException(message)