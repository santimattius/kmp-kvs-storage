package com.santimattius.kvs.internal.exception

import com.santimattius.kvs.KvsException

internal class RemoveKvsException(message: String) : KvsException(message)

internal class ClearKvsException(message: String) : KvsException(message)

internal class ContainsKvsException(message: String) : KvsException(message)
