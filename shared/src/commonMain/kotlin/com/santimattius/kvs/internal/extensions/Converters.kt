package com.santimattius.kvs.internal.extensions

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


/**
 * Encodes this [ByteArray] into a [String] using the Base64 encoding scheme.
 *
 * This extension function provides a convenient way to convert a byte array
 * into its Base64 string representation.
 *
 * @return The resulting Base64 encoded [String].
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.byteArrayToBase64(): String {
    return Base64.encode(this)
}

/**
 * Decodes this Base64-encoded [String] into a [ByteArray].
 *
 * This extension function provides a convenient way to convert a Base64 string
 * back into its original binary data representation.
 *
 * @return The resulting [ByteArray] from decoding the Base64 string.
 * @throws IllegalArgumentException if this is not a valid Base64-encoded string.
 */
@OptIn(ExperimentalEncodingApi::class)
fun String.base64ToByteArray(): ByteArray {
    return Base64.decode(this)
}