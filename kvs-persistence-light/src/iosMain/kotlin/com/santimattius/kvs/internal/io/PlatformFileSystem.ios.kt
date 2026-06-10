package com.santimattius.kvs.internal.io

import okio.FileSystem

internal actual fun platformFileSystem(): FileSystem = FileSystem.SYSTEM
