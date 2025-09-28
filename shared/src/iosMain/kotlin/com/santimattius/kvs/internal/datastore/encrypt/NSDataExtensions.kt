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


/**
 * Converts a [String] to an [NSData] object.
 *
 * This extension function takes a [String] and attempts to convert it into an [NSData]
 * object using UTF-8 encoding.
 *
 * @return An [NSData] object representing the string in UTF-8 encoding, or `null` if the
 * conversion fails.
 *
 * @suppress This annotation is used because the cast from [String] to [NSString] is
 * platform-specific (Kotlin/Native) and might appear as a "cast that never succeeds"
 * in a common code analysis that doesn't account for Kotlin/Native specifics.
 */
@Suppress("CAST_NEVER_SUCCEEDS")
fun String.asNSData(): NSData? {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}
/**
 * Converts an NSData object to a ByteArray.
 *
 * This function first converts the NSData object to a ByteString
 * using the `toByteString()` extension function. Then, it converts
 * the ByteString to a ByteArray using the `toByteArray()` method.
 *
 * @return A ByteArray representation of the NSData object.
 * @see toByteString
 */
@OptIn(ExperimentalForeignApi::class)
fun NSData.convertToByteArray(): ByteArray {
    // Convert NSData to ByteString
    val byteString: ByteString = this.toByteString()

    // Convert ByteString to ByteArray
    val byteArray: ByteArray = byteString.toByteArray()
    return byteArray
}

/**
 * Converts a Kotlin ByteArray to an NSData object.
 * This function uses `usePinned` to ensure the ByteArray's memory is pinned
 * during the NSData creation, preventing it from being moved by the garbage collector.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.asNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(
            bytes = pinned.addressOf(0),
            length = this.size.convert(),
        )
    }
}

/**
 * Converts an [NSData] object to a [String].
 *
 * This function first converts the [NSData] to a [ByteString], then to a [ByteArray],
 * and finally decodes the [ByteArray] to a [String] using UTF-8 encoding.
 *
 * @return The [String] representation of the [NSData].
 */
@OptIn(ExperimentalForeignApi::class)
fun NSData.asString(): String {

    // Convert NSData to ByteString
    val byteString: ByteString = this.toByteString()

    // Convert ByteString to ByteArray
    val byteArray: ByteArray = byteString.toByteArray()

    // Decode ByteArray to String using UTF-8 encoding
    return byteArray.decodeToString()
}