package com.santimattius.kvs.internal.document

import okio.FileSystem

internal actual fun platformFileSystem(): FileSystem = FileSystem.SYSTEM
