package com.santimattius.kvs.internal.datastore.encrypt

import android.util.Base64
import com.santimattius.kvs.internal.logger.KvsLogger

internal actual fun encryptor(key: String, logger: KvsLogger): Encryptor = AndroidEncryptor(key, logger)

private class AndroidEncryptor(key: String, private val logger: KvsLogger) : Encryptor {

    private val crypto = Crypto(key)

    override fun encrypt(input: String): String {
        return try {
            val encrypted = crypto.encrypt(input.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (ex: Throwable) {
            logger.error("encrypt error: ${ex.message}", ex)
            input
        }
    }

    override fun decrypt(input: String): String {
        return try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            crypto.decrypt(decoded).toString(Charsets.UTF_8)
        } catch (ex: Throwable) {
            logger.error("decrypt error: ${ex.message}", ex)
            input
        }
    }
}
