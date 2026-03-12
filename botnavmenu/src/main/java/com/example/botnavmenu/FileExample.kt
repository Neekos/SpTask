package com.example.botnavmenu

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

data class FileExample(
    val id: Int,
    val fileName: String
) {
    companion object {
        private const val FILES_DIRECTORY = "my_files"

        fun getFilesDirectory(context: Context): File {
            return File(context.filesDir, FILES_DIRECTORY).apply {
                if (!exists()) mkdirs()
            }
        }

        fun getAllFiles(context: Context): List<FileExample> {
            val directory = getFilesDirectory(context)
            return directory.listFiles()
                ?.filter { it.isFile }
                ?.sortedBy { it.name }
                ?.mapIndexed { index, file ->
                    FileExample(index + 1, file.name)
                } ?: emptyList()
        }

        fun createFile(context: Context, fileName: String): Boolean {
            val directory = getFilesDirectory(context)
            val file = File(directory,
                if (!fileName.endsWith(".txt")) "$fileName.txt" else fileName)
            return if (!file.exists()) {
                try {
                    file.createNewFile()
                    true
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }
    }

    fun getFile(context: Context): File {
        val directory = FileExample.getFilesDirectory(context)
        return File(directory, fileName)
    }

    fun exists(context: Context): Boolean {
        return getFile(context).exists()
    }

    fun readContent(context: Context): String {
        return try {
            val file = getFile(context)
            if (file.exists()) {
                FileInputStream(file).use { inputStream ->
                    inputStream.readBytes().toString(Charset.defaultCharset())
                }
            } else {
                ""
            }
        } catch (e: Exception) {
            "Ошибка чтения: ${e.message}"
        }
    }

    fun writeContent(context: Context, content: String): Boolean {
        return try {
            val file = getFile(context)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun delete(context: Context): Boolean {
        return try {
            getFile(context).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun rename(context: Context, newFileName: String): Boolean {
        return try {
            val oldFile = getFile(context)
            val directory = FileExample.getFilesDirectory(context)
            val newFile = File(directory, newFileName)
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    fun getSize(context: Context): String {
        val file = getFile(context)
        val size = file.length()

        return when {
            size < 1024 -> "$size байт"
            size < 1024 * 1024 -> "${size / 1024} КБ"
            else -> "${size / (1024 * 1024)} МБ"
        }
    }

    fun getLastModified(context: Context): String {
        val file = getFile(context)
        return if (file.exists()) {
            val lastModified = file.lastModified()
            android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", lastModified).toString()
        } else {
            "Не доступно"
        }
    }
}