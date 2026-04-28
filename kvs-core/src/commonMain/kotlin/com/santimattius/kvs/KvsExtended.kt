package com.santimattius.kvs

import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.exception.WriteKvsException
import com.santimattius.kvs.internal.ttl.CleanupJob
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration

@ExperimentalKvsTtl
interface KvsExtended : KvsStandard, KvsStream {

    interface KvsExtendedEditor {

        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String, duration: Duration): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int, duration: Duration): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long, duration: Duration): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float, duration: Duration): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean, duration: Duration): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun remove(key: String): KvsExtendedEditor

        @Throws(IllegalStateException::class)
        fun clear(): KvsExtendedEditor

        @Throws(IllegalStateException::class, WriteKvsException::class, CancellationException::class)
        suspend fun commit()
    }

    fun edit(): KvsExtendedEditor

    suspend operator fun contains(key: String): Boolean

    fun cleanupJob(interval: Duration): CleanupJob
}
