package com.santimattius.kvs.internal.datastore.encrypt

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import okio.ByteString
import okio.ByteString.Companion.toByteString
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding

@Suppress("CAST_NEVER_SUCCEEDS")
fun String.asNSData(): NSData? = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)

@OptIn(ExperimentalForeignApi::class)
fun NSData.convertToByteArray(): ByteArray = this.toByteString().toByteArray()

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.asNSData(): NSData = this.usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = this.size.convert())
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.asString(): String = this.toByteString().toByteArray().decodeToString()
