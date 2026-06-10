package com.santimattius.kvs.internal

import app.cash.sqldelight.db.SqlDriver

internal expect fun createSqlDriver(name: String): SqlDriver
