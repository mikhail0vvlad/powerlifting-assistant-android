package com.powerlifting.assistant.presentation.viewmodel

internal fun errorMessage(t: Throwable): String {
    val msg = t.message ?: ""
    return when {
        msg.contains("503") -> "Сервер временно недоступен. Повторите позже."
        msg.contains("Unable to resolve host") -> "Нет связи с сервером. Проверьте интернет."
        msg.contains("timeout", ignoreCase = true) -> "Время ожидания истекло. Повторите."
        msg.isNotBlank() -> msg
        else -> "Неизвестная ошибка"
    }
}
