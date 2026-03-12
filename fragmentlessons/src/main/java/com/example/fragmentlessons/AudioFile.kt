package com.example.fragmentlessons

import android.net.Uri

data class AudioFile(
    val id: Long,                // Уникальный ID (можно использовать hash имени)
    val displayName: String,     // Отображаемое имя (без расширения)
    val fileName: String,        // Полное имя файла с расширением
    val uri: Uri,                // URI для доступа к файлу
    val sourceType: AudioSource  // Тип источника
)

enum class AudioSource {
    RAW,      // Из ресурсов приложения
    EXTERNAL  // Из внешнего хранилища
}