package com.powerlifting.assistant.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Юнит-тесты для [errorMessage] — перевода технических исключений в русские сообщения.
 * Функция internal, поэтому тест лежит в том же модуле и пакете.
 */
class ErrorMapperTest {

    @Test
    fun `503 переводится в сообщение о недоступности сервера`() {
        val msg = errorMessage(RuntimeException("HTTP 503 Service Unavailable"))
        assertEquals("Сервер временно недоступен. Повторите позже.", msg)
    }

    @Test
    fun `Unable to resolve host переводится в сообщение про интернет`() {
        val msg = errorMessage(IOException("Unable to resolve host \"api.example.com\""))
        assertEquals("Нет связи с сервером. Проверьте интернет.", msg)
    }

    @Test
    fun `timeout переводится независимо от регистра`() {
        val expected = "Время ожидания истекло. Повторите."
        assertEquals(expected, errorMessage(RuntimeException("timeout")))
        assertEquals(expected, errorMessage(RuntimeException("Read TIMEOUT")))
    }

    @Test
    fun `прочее непустое сообщение возвращается как есть`() {
        val msg = errorMessage(IllegalStateException("Что-то пошло не так"))
        assertEquals("Что-то пошло не так", msg)
    }

    @Test
    fun `пустое или null сообщение даёт текст про неизвестную ошибку`() {
        assertEquals("Неизвестная ошибка", errorMessage(RuntimeException()))
        assertEquals("Неизвестная ошибка", errorMessage(RuntimeException("   ")))
    }

    @Test
    fun `проверка 503 имеет приоритет над общим сообщением`() {
        // Строка содержит и "503", и осмысленный текст — должно сработать правило 503.
        val msg = errorMessage(RuntimeException("Ошибка 503 на сервере"))
        assertEquals("Сервер временно недоступен. Повторите позже.", msg)
    }
}
