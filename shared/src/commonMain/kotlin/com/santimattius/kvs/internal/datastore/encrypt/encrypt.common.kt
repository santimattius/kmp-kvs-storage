package com.santimattius.kvs.internal.datastore.encrypt

import com.santimattius.kvs.internal.logger.KvsLogger

internal expect fun encryptor(key: String, logger: KvsLogger): Encryptor