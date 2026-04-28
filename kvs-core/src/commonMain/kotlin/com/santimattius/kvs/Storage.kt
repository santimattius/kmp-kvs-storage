package com.santimattius.kvs

import com.santimattius.kvs.internal.provideInMemoryKvsInstance

object Storage {

    private var isDebug = false

    fun debug(isDebug: Boolean) {
        this.isDebug = isDebug
    }

    fun inMemoryKvs(name: String): Kvs = provideInMemoryKvsInstance(name)
}
