package com.santimattius.kvs

import com.santimattius.kvs.internal.DataStoreKvs
import com.santimattius.kvs.internal.provideDataStoreInstance

object Storage {

    fun kvs(name: String): Kvs = DataStoreKvs(provideDataStoreInstance(name))
}