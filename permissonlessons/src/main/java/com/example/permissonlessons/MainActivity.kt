package com.example.permissonlessons

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // API 30: Все изменения API 29 остаются
        // ДОПОЛНИТЕЛЬНО: Управление всеми файлами требует специального разрешения
        checkPermissionsApi30()

        // Кнопка ОТКРЫТЬ - открывает любой файл
        findViewById<Button>(R.id.btn_open).setOnClickListener {
            openFile()
        }

        // Кнопка СОХРАНИТЬ - сохраняет тестовый файл
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            saveFile()
        }
    }

    /**
     * API 30: РАЗРЕШЕНИЯ
     */
    private fun checkPermissionsApi30() {
        // Для доступа к медиафайлам (фото, видео, музыка)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: проверяем доступ к медиа
            val mediaPermission = Manifest.permission.READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(this, mediaPermission)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(mediaPermission), 100)
            }

            // ДОПОЛНИТЕЛЬНО: Для полного доступа ко всем файлам (опционально)
            // Это нужно только если хотим сохранять в любые папки
            // checkAllFilesAccess()
        } else {
            // Для Android 10 и ниже
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 100)
            }
        }
    }

    /**
     * Проверка доступа ко всем файлам (MANAGE_EXTERNAL_STORAGE)
     * Нужно только для файловых менеджеров
     */
    private fun checkAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // Можно запросить, но это не обязательно для базовых операций
                // requestAllFilesAccess()
            }
        }
    }

    /**
     * Запрос полного доступа ко всем файлам
     */
    private fun requestAllFilesAccess() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:${packageName}")
        startActivity(intent)
    }

    /**
     * КНОПКА "ОТКРЫТЬ" - открывает любой файл
     * Используем ACTION_OPEN_DOCUMENT для API 30+
     */
    private fun openFile() {
        // Используем ACTION_OPEN_DOCUMENT - дает постоянный доступ
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Все типы файлов
        }

        startActivityForResult(
            Intent.createChooser(intent, "Выберите файл"),
            101 // REQUEST_OPEN_FILE
        )
    }

    /**
     * КНОПКА "СОХРАНИТЬ" - сохраняет тестовый файл
     * На API 30 используем MediaStore
     */
    private fun saveFile() {
        // Создаем тестовое изображение
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(100, 200, 100)) // Зеленый цвет

        // Сохраняем в папку Pictures через MediaStore
        val uri = saveImageToMediaStore(
            bitmap,
            "test_image_${System.currentTimeMillis()}.jpg"
        )

        if (uri != null) {
            Toast.makeText(this, "Файл сохранен!", Toast.LENGTH_SHORT).show()

            // Показываем сохраненное изображение
            findViewById<ImageView>(R.id.image_view)?.setImageURI(uri)
        } else {
            Toast.makeText(this, "Не удалось сохранить файл", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Сохранение изображения через MediaStore (API 29+)
     */
    private fun saveImageToMediaStore(bitmap: Bitmap, fileName: String): Uri? {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

            // Для API 29+ указываем RELATIVE_PATH
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/MyApp")
            }
        }
        // Вставляем запись в MediaStore
        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        // Сохраняем данные
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    return uri
                }
            } catch (e: Exception) {
                Log.e("SaveFile", "Ошибка сохранения", e)
            }
        }
        return null
    }

    /**
     * Обработка выбранного файла
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                101 -> { // Выбран файл через кнопку "Открыть"
                    data?.data?.let { uri ->
                        // Сохраняем право на постоянный доступ к файлу
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                        // Пытаемся получить постоянный доступ
                        try {
                            contentResolver.takePersistableUriPermission(uri, takeFlags)
                            Toast.makeText(this, "Получен постоянный доступ к файлу",
                                Toast.LENGTH_SHORT).show()
                        } catch (e: SecurityException) {
                            // Файл не поддерживает постоянный доступ - это нормально
                        }

                        // Показываем файл
                        showSelectedFile(uri)
                    }
                }
            }
        }
    }

    /**
     * Показываем выбранный файл
     */
    private fun showSelectedFile(uri: Uri) {
        try {
            // Определяем тип файла
            val mimeType = contentResolver.getType(uri)

            // Пытаемся показать как изображение
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    findViewById<ImageView>(R.id.image_view)?.setImageBitmap(bitmap)
                    Toast.makeText(this, "Открыто изображение", Toast.LENGTH_SHORT).show()
                } else {
                    // Если не изображение, показываем информацию
                    Toast.makeText(this,
                        "Открыт файл типа: $mimeType",
                        Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка открытия файла: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Обработка ответа на запрос разрешений
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение получено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "Без разрешения некоторые функции могут не работать",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}