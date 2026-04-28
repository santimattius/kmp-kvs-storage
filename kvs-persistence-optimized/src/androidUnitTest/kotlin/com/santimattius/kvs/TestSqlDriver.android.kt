package com.santimattius.kvs

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.santimattius.kvs.persistence.optimized.db.KvsDatabase

actual fun createTestSqlDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    KvsDatabase.Schema.create(driver)
    return driver
}
