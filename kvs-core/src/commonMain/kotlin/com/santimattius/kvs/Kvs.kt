package com.santimattius.kvs

import com.santimattius.kvs.internal.KvsStandard
import com.santimattius.kvs.internal.KvsStream
import com.santimattius.kvs.internal.exception.WriteKvsException
import kotlinx.coroutines.CancellationException

interface Kvs : KvsStandard, KvsStream {

    interface KvsEditor {

        @Throws(IllegalStateException::class)
        fun putString(key: String, value: String): KvsEditor

        @Throws(IllegalStateException::class)
        fun putInt(key: String, value: Int): KvsEditor

        @Throws(IllegalStateException::class)
        fun putLong(key: String, value: Long): KvsEditor

        @Throws(IllegalStateException::class)
        fun putFloat(key: String, value: Float): KvsEditor

        @Throws(IllegalStateException::class)
        fun putBoolean(key: String, value: Boolean): KvsEditor

        @Throws(IllegalStateException::class)
        fun remove(key: String): KvsEditor

        @Throws(IllegalStateException::class)
        fun clear(): KvsEditor

        @Throws(IllegalStateException::class, WriteKvsException::class, CancellationException::class)
        suspend fun commit()
    }

    fun edit(): KvsEditor

    suspend operator fun contains(key: String): Boolean
}
