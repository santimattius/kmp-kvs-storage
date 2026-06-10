package com.santimattius.kvs.internal.ttl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface CleanupJob {
    fun start(scope: CoroutineScope): Job
}
