package com.santimattius.kvs.internal

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.santimattius.kvs.internal.getOptimizedContext
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase

internal actual fun createSqlDriver(name: String): SqlDriver =
    AndroidSqliteDriver(KvsDatabase.Schema, getApplicationContext(), "$name.db")
