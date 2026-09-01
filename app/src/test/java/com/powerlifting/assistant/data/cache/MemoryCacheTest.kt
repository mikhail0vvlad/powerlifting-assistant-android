package com.powerlifting.assistant.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Юнит-тесты для [MemoryCache] — потокобезопасного TTL-кэша.
 * Проверяем выдачу свежих значений, истечение TTL и инвалидацию.
 */
class MemoryCacheTest {

    @Test
    fun `свежее значение возвращается`() {
        val cache = MemoryCache<String, Int>(5.minutes)
        cache.put("a", 42)
        assertEquals(42, cache.get("a"))
    }

    @Test
    fun `отсутствующий ключ возвращает null`() {
        val cache = MemoryCache<String, Int>(5.minutes)
        assertNull(cache.get("missing"))
    }

    @Test
    fun `значение истекает после TTL`() {
        val cache = MemoryCache<String, Int>(20.milliseconds)
        cache.put("a", 42)
        // Ждём чуть дольше TTL — запись должна протухнуть и вернуться null.
        Thread.sleep(60)
        assertNull(cache.get("a"))
    }

    @Test
    fun `invalidate удаляет конкретный ключ`() {
        val cache = MemoryCache<String, Int>(5.minutes)
        cache.put("a", 1)
        cache.put("b", 2)

        cache.invalidate("a")

        assertNull(cache.get("a"))
        assertEquals(2, cache.get("b"))
    }

    @Test
    fun `invalidateAll очищает кэш целиком`() {
        val cache = MemoryCache<String, Int>(5.minutes)
        cache.put("a", 1)
        cache.put("b", 2)

        cache.invalidateAll()

        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }

    @Test
    fun `put перезаписывает прежнее значение`() {
        val cache = MemoryCache<String, Int>(5.minutes)
        cache.put("a", 1)
        cache.put("a", 99)
        assertEquals(99, cache.get("a"))
    }
}
