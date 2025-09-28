package com.santimattius.kvs.internal.extensions

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.byteArrayToBase64(): String {
    return Base64.encode(this)
}

@OptIn(ExperimentalEncodingApi::class)
fun String.base64ToByteArray(): ByteArray {
    return Base64.decode(this)
}