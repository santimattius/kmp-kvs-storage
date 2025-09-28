package com.santimattius.kvs.internal.datastore.encrypt

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Provides encryption and decryption functionalities using Android KeyStore.
 * @param keyAlias The alias for the key in the KeyStore. Defaults to "secret".
 */
internal class Crypto(
    private val keyAlias: String = KEY_ALIAS
) {
    companion object {
        private const val KEY_ALIAS = "secret"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private val cipher = Cipher.getInstance(TRANSFORMATION)
        private val keyStore = KeyStore
            .getInstance("AndroidKeyStore")
            .apply {
                load(null)
            }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore
            .getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator
            .getInstance(ALGORITHM)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_ENCRYPT or
                                KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        .setRandomizedEncryptionRequired(true)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }
            .generateKey()
    }

    /**
     * Encrypts the given byte array.
     * @param bytes The byte array to encrypt.
     * @return The encrypted byte array, prefixed with the initialization vector (IV).
     */
    fun encrypt(bytes: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(bytes)
        return iv + encrypted
    }

    /**
     * Decrypts the given byte array.
     * @param bytes The byte array to decrypt. It's assumed that the IV is prefixed to the data.
     * @return The decrypted byte array.
     */
    fun decrypt(bytes: ByteArray): ByteArray {
        val iv = bytes.copyOfRange(0, cipher.blockSize)
        val data = bytes.copyOfRange(cipher.blockSize, bytes.size)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }
}
