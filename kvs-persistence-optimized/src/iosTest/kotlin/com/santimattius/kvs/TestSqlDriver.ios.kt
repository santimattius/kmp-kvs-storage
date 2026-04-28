package com.santimattius.kvs

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase

actual fun createTestSqlDriver(): SqlDriver = NativeSqliteDriver(KvsDatabase.Schema, ":memory:")
