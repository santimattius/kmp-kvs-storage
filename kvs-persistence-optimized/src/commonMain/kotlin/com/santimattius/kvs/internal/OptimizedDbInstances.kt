package com.santimattius.kvs.internal

import com.santimattius.kvs.persistence.optimized.db.KvsDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

@OptIn(InternalCoroutinesApi::class)
private val lock = SynchronizedObject()
private val databases = mutableMapOf<String, KvsDatabase>()

@OptIn(InternalCoroutinesApi::class)
internal fun provideDatabase(name: String): KvsDatabase = synchronized(lock) {
    databases.getOrPut(name) {
        val driver = createSqlDriver(name)
        KvsDatabase(driver)
    }
}
