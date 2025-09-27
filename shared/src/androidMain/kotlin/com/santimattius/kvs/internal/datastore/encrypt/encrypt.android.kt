package com.santimattius.kvs.internal.datastore.encrypt

import android.util.Base64

actual fun encryptor(): Encryptor {
    return AndroidEncryptor()
}

private class AndroidEncryptor : Encryptor {

    override fun encrypt(input: String): String {
        val encrypt = Crypto.encrypt(input.toByteArray())
        return Base64.encodeToString(encrypt, Base64.DEFAULT)
    }

    override fun decrypt(input: String): String {
        val decoded = Base64.decode(input, Base64.DEFAULT)
        return Crypto.decrypt(decoded).toString(Charsets.UTF_8)
    }
}