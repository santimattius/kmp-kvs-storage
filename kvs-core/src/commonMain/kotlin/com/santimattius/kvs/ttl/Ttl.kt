package com.santimattius.kvs.ttl

import com.santimattius.kvs.ExperimentalKvsTtl

@ExperimentalKvsTtl
interface Ttl {
    fun value(): Long
}
