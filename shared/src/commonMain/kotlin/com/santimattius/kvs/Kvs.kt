package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.provideDataStoreInstance

interface Kvs {

    interface KvsEditor {

        fun putString(key: String, value: String): KvsEditor

        fun putInt(key: String, value: Int): KvsEditor

        fun putLong(key: String, value: Long): KvsEditor

        fun putFloat(key: String, value: Float): KvsEditor

        fun putBoolean(key: String, value: Boolean): KvsEditor

        fun remove(key: String): KvsEditor

        fun clear(): KvsEditor

        suspend fun commit()

    }


    suspend fun getAll(): Map<String, Any>

    suspend fun getString(key: String, defValue: String): String

    suspend fun getInt(key: String, defValue: Int): Int

    suspend fun getLong(key: String, defValue: Long): Long

    suspend fun getFloat(key: String, defValue: Float): Float

    suspend fun getBoolean(key: String, defValue: Boolean): Boolean

    fun edit(): KvsEditor

    suspend operator fun contains(key: String): Boolean

}