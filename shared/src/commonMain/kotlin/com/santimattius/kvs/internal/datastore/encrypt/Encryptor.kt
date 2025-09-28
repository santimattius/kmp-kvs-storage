package com.santimattius.kvs.internal.datastore.encrypt

/**
 * Interface for encrypting and decrypting strings.
 */
interface Encryptor {

    /**
     * Encrypts the given string.
     * @param input The string to encrypt.
     * @return The encrypted string.
     */
    fun encrypt(input: String): String

    /**
     * Decrypts the given string.
     * @param input The string to decrypt.
     * @return The decrypted string.
     */
    fun decrypt(input: String): String

    /**
     * A no-op implementation of [Encryptor] that does not perform any encryption or decryption.
     */
    companion object None : Encryptor {
        override fun encrypt(input: String): String = input
        override fun decrypt(input: String): String = input
    }

}