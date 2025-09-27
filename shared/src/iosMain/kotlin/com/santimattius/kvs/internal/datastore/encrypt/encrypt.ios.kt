package com.santimattius.kvs.internal.datastore.encrypt

import com.santimattius.kvs.native.KtCrypto
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

actual fun encryptor(): Encryptor {
    return IosEncryptor()
}

private class IosEncryptor : Encryptor {
    @OptIn(ExperimentalForeignApi::class)
    private val crypto = KtCrypto()

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun encrypt(input: String): String {
        val nsData = input.nsdata() ?: throw Throwable("")
        return crypto.encryptWithInput(input = nsData, key = "").string().orEmpty()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun decrypt(input: String): String {
        //TODO: throw typed exception
        val nsData = input.nsdata() ?: throw Throwable("")
        return crypto.decryptWithInput(input = nsData, key = "").string().orEmpty()
    }

}

@Suppress("CAST_NEVER_SUCCEEDS")
fun String.nsdata(): NSData? {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}

@OptIn(BetaInteropApi::class)
fun NSData.string(): String? {
    return NSString.create(this, NSUTF8StringEncoding) as String?
}