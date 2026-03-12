package com.example.imagesmodul

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvStorageInfo: TextView

    private val imageResources = listOf(
        R.drawable.img,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_background
    )

    private var currentImageIndex = 0

    // Лаунчер для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Разрешения получены - выполняем отложенное действие
            when (pendingAction) {
                ACTION_SAVE_TO_DOWNLOADS -> saveToDownloads()
                ACTION_UPDATE_STORAGE -> updateStorageInfo()
            }
        } else {
            showToast(" Разрешения отклонены. Функции сохранения недоступны.")
        }
    }

    companion object {
        private const val ACTION_SAVE_TO_DOWNLOADS = "save_to_downloads"
        private const val ACTION_UPDATE_STORAGE = "update_storage"
    }
    private var pendingAction: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }





        val clickText: TextView = findViewById(R.id.textT)
        clickText.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        initializeViews()
        setupClickListeners()
        loadCurrentImage()
        checkPermissionsAndUpdateStorage()
    }

    private fun initializeViews() {
        imageView = findViewById(R.id.imageView)
        tvStorageInfo = findViewById(R.id.tvStorageInfo)
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnFromResources).setOnClickListener {
            loadCurrentImage()
        }
        findViewById<Button>(R.id.btnNextImage).setOnClickListener {
            showNextImage()
        }
        findViewById<Button>(R.id.btnSaveToDownloads).setOnClickListener {
            checkPermissionsAndSave()
        }
        findViewById<Button>(R.id.btnShowInExplorer).setOnClickListener {
            showDownloadInstructions()
        }
        findViewById<Button>(R.id.btnCheckStorage).setOnClickListener {
            checkPermissionsAndUpdateStorage()
        }
        findViewById<Button>(R.id.btnRequestPermissions).setOnClickListener {
            requestPermissions()
        }

        findViewById<Button>(R.id.btnLoadFromDownloads).setOnClickListener {
            loadFromDownloads()
        }

        findViewById<Button>(R.id.btnLoadFromInternet).setOnClickListener {
            //showInternetImageDialog()
            loadImageFromUrl()
        }

        findViewById<Button>(R.id.btnAssets).setOnClickListener {
            loadImageFromAssets()
        }

    }

    // ==================== ЗАГРУЗКА ИЗ ИНТЕРНЕТА ====================
    private fun showInternetImageDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "https://i.pinimg.com/originals/c2/df/e2/c2dfe29f24b61d2b8898250e067cd984.png"
            setText("https://i.pinimg.com/originals/c2/df/e2/c2dfe29f24b61d2b8898250e067cd984.png")
        }

        AlertDialog.Builder(this)
            .setTitle("Загрузка изображения из интернета")
            .setMessage("Введите URL изображения:")
            .setView(editText)
            .setPositiveButton("Загрузить") { _, _ ->
                val url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    loadImageFromInternet(url)
                } else {
                    showToast("Введите URL")
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadImageFromInternet(imageUrl: String) {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    requestMethod = "GET"
                    doInput = true
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    connection.disconnect()

                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                            showToast("Изображение загружено из интернета")

                            // Автоматически сохраняем в Downloads
                            saveDownloadedImage(bitmap, getFileNameFromUrl(imageUrl))
                        } else {
                            showToast("Не удалось декодировать изображение")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        showToast(" Ошибка сервера: ${connection.responseCode}")
                    }
                }

            } catch (e: java.net.SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast(" Таймаут соединения")
                }
            } catch (e: java.net.UnknownHostException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast(" Нет интернета или неверный URL")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast(" Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun loadImageFromUrl() {
        val imageUrl = "https://i.pinimg.com/originals/c2/df/e2/c2dfe29f24b61d2b8898250e067cd984.png"

        // Используем Glide или Picasso для загрузки
        Glide.with(this)
            .load(imageUrl)
            //.placeholder(R.drawable.placeholder) // Заглушка во время загрузки
            //.error(R.drawable.error) // Изображение при ошибке
            //.fallback(R.drawable.fallback) // Если URL null или пустой
            .override(300, 300) // Принудительный размер
            .centerCrop() // Масштабирование
            //.circleCrop() // Круглое изображение
            //.diskCacheStrategy(DiskCacheStrategy.ALL) // Стратегия кэширования
            //.priority(Priority.HIGH) // Приоритет загрузки
            .into(imageView)
    }

    private fun getFileNameFromUrl(url: String): String {
        return try {
            val fileName = URL(url).path.substringAfterLast('/')
            if (fileName.contains('.')) {
                "internet_$fileName"
            } else {
                "internet_image_${System.currentTimeMillis()}.jpg"
            }
        } catch (e: Exception) {
            "internet_image_${System.currentTimeMillis()}.jpg"
        }
    }

//    ======================== assets
private fun loadImageFromAssets() {
    try {
        val inputStream = assets.open("images/img.png")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        imageView.setImageBitmap(bitmap)
        inputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

    private fun saveDownloadedImage(bitmap: Bitmap, fileName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (hasStoragePermissions()) {
                    saveToDownloadsFolder(bitmap, fileName)
                    runOnUiThread {
                        showToast("💾 Изображение сохранено в Downloads")
                        updateStorageInfo()
                    }
                } else {
                    // Сохраняем в кэш если нет разрешений
                    saveToCache(bitmap, fileName)
                    runOnUiThread {
                        showToast("💾 Изображение сохранено в кэш")
                        updateStorageInfo()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка сохранения", e)
            }
        }
    }

    private fun saveToCache(bitmap: Bitmap, fileName: String) {
        val cacheFile = File(externalCacheDir, fileName)
        FileOutputStream(cacheFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
    }

    // Добавьте ProgressBar в layout и этот метод для показа загрузки
    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility =
            if (show) View.VISIBLE else View.GONE
    }

    // ==================== ЗАГРУЗКА ИЗ DOWNLOADS ====================
    private fun loadFromDownloads() {
        if (!hasStoragePermissions()) {
            showToast("⚠ Сначала предоставьте разрешения")
            requestPermissions()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val files = getDownloadFiles()

                if (files.isNotEmpty()) {
                    // Сортируем по дате изменения (новые первыми)
                    val sortedFiles = files.sortedByDescending { it.lastModified }
                    val latestFile = sortedFiles.first()

                    runOnUiThread {
                        showFileSelectionDialog(sortedFiles)
                    }
                } else {
                    runOnUiThread {
                        showToast(" В папке Downloads нет изображений")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast(" Ошибка загрузки: ${e.message}")
                }
            }
        }
    }

    private fun showFileSelectionDialog(files: List<FileInfo>) {
        val fileNames = files.map {
            "${it.name} (${formatFileSize(it.size)}) - ${formatDate(it.lastModified)}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите изображение для загрузки")
            .setItems(fileNames) { _, which ->
                val selectedFile = files[which]
                loadSelectedFile(selectedFile)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun loadSelectedFile(fileInfo: FileInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(fileInfo.path)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

                    runOnUiThread {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                            showToast("✅ Загружено: ${fileInfo.name}")

                            // Сохраняем как текущее изображение для возможного повторного сохранения
                            saveBitmapToCache(bitmap, "loaded_${fileInfo.name}")
                        } else {
                            showToast(" Не удалось загрузить изображение")
                        }
                    }
                } else {
                    runOnUiThread {
                        showToast(" Файл не найден: ${fileInfo.name}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast(" Ошибка: ${e.message}")
                }
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap, fileName: String) {
        try {
            val cacheFile = File(externalCacheDir, fileName)
            FileOutputStream(cacheFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка сохранения в кэш", e)
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    // Обновленный метод getDownloadFiles для лучшей фильтрации
    private fun getDownloadFiles(): List<FileInfo> {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.listFiles()?.filter { file ->
                file.isFile && isImageFile(file.name)
            }?.map { file ->
                FileInfo(file.name, file.length(), file.absolutePath, file.lastModified())
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Расширенная проверка изображений
    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp")
        val lowerFileName = fileName.lowercase()
        return imageExtensions.any { lowerFileName.endsWith(it) }
    }


    // ==================== ПРОВЕРКА И ЗАПРОС РАЗРЕШЕНИЙ ====================
    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionsAndSave() {
        if (hasStoragePermissions()) {
            saveToDownloads()
        } else {
            pendingAction = ACTION_SAVE_TO_DOWNLOADS
            showPermissionDialog()
        }
    }

    private fun checkPermissionsAndUpdateStorage() {
        if (hasStoragePermissions()) {
            updateStorageInfo()
        } else {
            pendingAction = ACTION_UPDATE_STORAGE
            updateStorageInfo() // Все равно обновим, но покажем предупреждение
        }
    }

    private fun requestPermissions() {
        pendingAction = ACTION_UPDATE_STORAGE
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(" Требуются разрешения")
            .setMessage("Для сохранения файлов в папку Downloads необходимо предоставить разрешение на доступ к хранилищу.")
            .setPositiveButton("Предоставить") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Отмена") { _, _ ->
                showToast("Разрешения отклонены")
            }
            .show()
    }

    // ==================== РАБОТА С ИЗОБРАЖЕНИЯМИ ====================
    private fun loadCurrentImage() {
        if (imageResources.isNotEmpty()) {
            val resourceId = imageResources[currentImageIndex]
            imageView.setImageResource(resourceId)
            showToast("Загружено изображение ${currentImageIndex + 1} из ресурсов")
            updateStorageInfo()
        }
    }

    private fun showNextImage() {
        if (imageResources.size > 1) {
            currentImageIndex = (currentImageIndex + 1) % imageResources.size
            loadCurrentImage()
        } else {
            showToast("Только одно изображение в ресурсах")
        }
    }

    private fun saveToDownloads() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resourceId = imageResources[currentImageIndex]
                val bitmap = BitmapFactory.decodeResource(resources, resourceId)

                if (bitmap != null) {
                    val fileName = "image_${currentImageIndex + 1}_${System.currentTimeMillis()}.jpg"
                    saveToDownloadsFolder(bitmap, fileName)

                    runOnUiThread {
                        showToast("✅ Изображение сохранено в Downloads!")
                        updateStorageInfo()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast("❌ Ошибка сохранения: ${e.message}")
                }
            }
        }
    }

    private fun saveToDownloadsFolder(bitmap: Bitmap, fileName: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        // Обновляем медиа-хранилище
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        sendBroadcast(mediaScanIntent)
    }

    // ==================== ИНФОРМАЦИЯ О ХРАНИЛИЩЕ ====================
    private fun updateStorageInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val hasPermissions = hasStoragePermissions()
            val downloadFiles = if (hasPermissions) getDownloadFiles() else emptyList()

            runOnUiThread {
                val info = buildString {
                    append(" ТЕКУЩЕЕ ИЗОБРАЖЕНИЕ: ${currentImageIndex + 1}/${imageResources.size}\n\n")

                    append(" РАЗРЕШЕНИЯ: ")
                    append(if (hasPermissions) " ПРЕДОСТАВЛЕНЫ" else " ОТСУТСТВУЮТ")
                    append("\n\n")

                    if (hasPermissions) {
                        append(" ФАЙЛЫ В DOWNLOADS:\n")
                        if (downloadFiles.isNotEmpty()) {
                            downloadFiles.forEachIndexed { index, file ->
                                append("${index + 1}. ${file.name}\n")
                                append("    ${file.size} байт\n")
                                append("    ${getFileTime(file)}\n\n")
                            }
                        } else {
                            append(" Нет сохраненных файлов\n\n")
                        }
                    } else {
                        append("⚠ Для просмотра файлов предоставьте разрешения\n\n")
                    }

                    append(" Нажмите ' Сохранить в Downloads' чтобы создать файлы")
                }

                tvStorageInfo.text = info
            }
        }
    }


    private fun getFileTime(file: FileInfo): String {
        return try {
            val date = Date(file.lastModified)
            val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "неизвестно"
        }
    }

    private fun showDownloadInstructions() {
        val info = """
             Файлы сохраняются в:
            /storage/emulated/0/Download/
            
             Как скачать с эмулятора:
            
            1. Откройте Android Studio
            2. View → Tool Windows → Device Explorer
            3. Перейдите в папку: /storage/emulated/0/Download/
            4. Найдите файлы: image_*.jpg
            5. Правой кнопкой → Save As
            
             Через ADB команды:
            adb pull /storage/emulated/0/Download/image_*.jpg ./
            
            ⚠ Сначала предоставьте разрешения!
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Инструкция по скачиванию")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

data class FileInfo(
    val name: String,
    val size: Long,
    val path: String,
    val lastModified: Long = 0
)