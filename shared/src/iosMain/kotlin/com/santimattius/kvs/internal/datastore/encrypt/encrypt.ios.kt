package com.santimattius.kvs.internal.datastore.encrypt

import com.santimattius.kvs.internal.extensions.base64ToByteArray
import com.santimattius.kvs.internal.extensions.byteArrayToBase64
import com.santimattius.kvs.internal.logger.KvsLogger
import com.santimattius.kvs.native.KtCrypto
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData

internal actual fun encryptor(key: String, logger: KvsLogger): Encryptor {
    return IosEncryptor(key = key, logger = logger)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosEncryptor(
    private val key: String,
    private val logger: KvsLogger
) : Encryptor {
    @OptIn(ExperimentalForeignApi::class)
    private val crypto = KtCrypto()


    override fun encrypt(input: String): String {
        try {
            val nsData = input.asNSData() ?: throw EncryptException("Error convert to NSData")
            val encryptWithInput: NSData = crypto.encryptWithInput(input = nsData, key = key)
            val byteArray = encryptWithInput.convertToByteArray()
            val output = byteArray.byteArrayToBase64()
            return output
        } catch (e: Throwable) {
            logger.error("encrypt error: ${e.message}", e)
            return input
        }
    }

    override fun decrypt(input: String): String {
        try {
            val byteArray: ByteArray = input.base64ToByteArray()
            val nsData = byteArray.asNSData()
            val decryptWithInput: NSData = crypto.decryptWithInput(input = nsData, key = key)
            val output = decryptWithInput.asString()
            return output
        } catch (e: Throwable) {
            logger.error("decrypt error: ${e.message}", e)
            return input
        }
    }
}