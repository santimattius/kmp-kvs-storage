package com.santimattius.kvs.internal

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase

internal actual fun createSqlDriver(name: String): SqlDriver =
    NativeSqliteDriver(KvsDatabase.Schema, "$name.db")
