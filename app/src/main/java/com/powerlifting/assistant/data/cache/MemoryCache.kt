package com.powerlifting.assistant.data.cache

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

class MemoryCache<K : Any, V : Any>(private val ttl: Duration) {

    private data class Entry<V>(val value: V, val expiresAt: Long)

    private val map = ConcurrentHashMap<K, Entry<V>>()

    fun get(key: K): V? {
        val entry = map[key] ?: return null
        if (System.currentTimeMillis() >= entry.expiresAt) {
            map.remove(key, entry)
            return null
        }
        return entry.value
    }

    fun put(key: K, value: V) {
        map[key] = Entry(value, System.currentTimeMillis() + ttl.inWholeMilliseconds)
    }

    fun invalidate(key: K) {
        map.remove(key)
    }

    fun invalidateAll() {
        map.clear()
    }
}
