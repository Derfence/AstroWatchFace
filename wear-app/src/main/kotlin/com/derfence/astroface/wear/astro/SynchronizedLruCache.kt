package com.derfence.astroface.wear.astro

internal class SynchronizedLruCache<K, V>(
    private val maxEntries: Int
) {
    init {
        require(maxEntries > 0) { "maxEntries must be positive." }
    }

    private val values = object : LinkedHashMap<K, V>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean =
            size > maxEntries
    }

    @Synchronized
    fun get(key: K): V? = values[key]

    @Synchronized
    fun getOrPut(key: K, producer: () -> V): V =
        values[key] ?: producer().also { values[key] = it }

    @Synchronized
    fun firstOrNull(predicate: (K, V) -> Boolean): V? =
        values.entries.firstOrNull { predicate(it.key, it.value) }?.value

    @Synchronized
    fun clear() {
        values.clear()
    }
}
