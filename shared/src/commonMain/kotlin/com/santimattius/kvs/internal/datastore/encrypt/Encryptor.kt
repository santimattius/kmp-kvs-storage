package com.santimattius.kvs.internal.datastore.encrypt

interface Encryptor {

    fun encrypt(input: String): String

    fun decrypt(input: String): String

    companion object None: Encryptor{
        override fun encrypt(input: String): String = input
        override fun decrypt(input: String): String = input
    }

}