package com.santimattius.kvs.internal.ttl

import com.santimattius.kvs.ExperimentalKvsTtl

@ExperimentalKvsTtl
interface Ttl {
    fun value(): Long
}
