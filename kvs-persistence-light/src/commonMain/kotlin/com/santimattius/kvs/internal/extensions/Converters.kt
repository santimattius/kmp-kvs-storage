package com.santimattius.kvs.internal.extensions

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.byteArrayToBase64(): String = Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.base64ToByteArray(): ByteArray = Base64.decode(this)
