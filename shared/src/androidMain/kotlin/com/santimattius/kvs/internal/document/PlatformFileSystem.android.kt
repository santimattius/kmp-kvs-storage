package com.santimattius.kvs.internal.document

import okio.FileSystem

actual fun platformFileSystem(): FileSystem {
    return FileSystem.SYSTEM
}