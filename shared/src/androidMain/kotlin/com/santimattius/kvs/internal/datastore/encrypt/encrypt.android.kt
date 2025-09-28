package com.santimattius.kvs.internal.datastore.encrypt

import android.util.Base64
import com.santimattius.kvs.internal.logger.KvsLogger

internal actual fun encryptor(key: String, logger: KvsLogger): Encryptor {
    return AndroidEncryptor(key = key, logger = logger)
}

private class AndroidEncryptor(
    key: String,
    private val logger: KvsLogger
) : Encryptor {
    private val crypto = Crypto(key)

    override fun encrypt(input: String): String {
        try {
            val encrypt = crypto.encrypt(input.toByteArray())
            return Base64.encodeToString(encrypt, Base64.DEFAULT)
        } catch (ex: Throwable) {
            logger.error("encrypt error: ${ex.message}", ex)
            return input
        }
    }

    override fun decrypt(input: String): String {
        try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            return crypto.decrypt(decoded).toString(Charsets.UTF_8)
        } catch (ex: Throwable) {
            logger.error("decrypt error: ${ex.message}", ex)
            return input
        }
    }
}