package com.example.fragmentlessons

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.fragmentlessons.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dataModel: DataModel by viewModels()

    // Список разрешений для разных версий Android
    private val permissionsForApi29AndBelow = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val permissionsForApi33AndAbove = arrayOf(
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    // Activity Result Launcher для новых API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настраиваем Navigation
        val navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNav.setupWithNavController(navController)

        // Проверяем и запрашиваем разрешения
        checkAndRequestPermissions()
    }

    fun checkAndRequestPermissions() {
        val permissionsToRequest = getRequiredPermissions()

        if (permissionsToRequest.isEmpty()) {
            // Все разрешения уже получены
            Toast.makeText(this, "Разрешения уже получены", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, нужно ли показывать объяснение
        if (shouldShowPermissionRationale(permissionsToRequest)) {
            showPermissionExplanationDialog(permissionsToRequest)
        } else {
            requestPermissions(permissionsToRequest)
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            permissionsForApi33AndAbove.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 (API 29-32)
            // Для Scoped Storage (API 29+) доступ через MediaStore
            // Можем работать без разрешений или запросить READ_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                permissionsForApi29AndBelow.filter {
                    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                }.toTypedArray()
            } else {
                emptyArray() // Для API 31-32 разрешения не требуются
            }
        } else {
            // Android 9 и ниже (API 28 и ниже)
            permissionsForApi29AndBelow.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()
        }
    }

    private fun shouldShowPermissionRationale(permissions: Array<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
    }

    private fun showPermissionExplanationDialog(permissions: Array<String>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Нужны разрешения")
            .setMessage("""
                Для работы с медиафайлами на вашем устройстве 
                необходимо предоставить разрешения.
                
                Приложение может:
                • 📁 Получать доступ к вашей музыке
                • 🎬 Получать доступ к вашим видео
                • 🔊 Воспроизводить медиафайлы
                
                Без разрешений будут доступны только 
                встроенные демо-файлы.
            """.trimIndent())
            .setPositiveButton("Дать разрешения") { _, _ ->
                requestPermissions(permissions)
            }
            .setNegativeButton("Позже") { _, _ ->
                Toast.makeText(this,
                    "Можно будет дать разрешения в настройках",
                    Toast.LENGTH_LONG).show()
                // Запускаем приложение в ограниченном режиме
                navigateToHome()
            }
            .setCancelable(false)
            .show()
    }

    private fun requestPermissions(permissions: Array<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Для Android 6.0+ используем новый API
            requestPermissionLauncher.launch(permissions)
        } else {
            // Для старых версий используем старый API
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            Toast.makeText(this, "Разрешения получены! 👍", Toast.LENGTH_SHORT).show()
            navigateToHome()
        } else {
            Toast.makeText(this,
                "Некоторые функции будут недоступны ⚠️\n" +
                        "Вы можете дать разрешения в настройках приложения",
                Toast.LENGTH_LONG).show()
            navigateToHome() // Все равно запускаем
        }
    }

    // Для старых версий Android
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allGranted) {
                Toast.makeText(this, "Разрешения получены!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,
                    "Будут доступны только встроенные файлы",
                    Toast.LENGTH_LONG).show()
            }

            navigateToHome()
        }
    }

    private fun navigateToHome() {
        // Navigation Component уже настроен, он сам покажет стартовый фрагмент
        // Из nav_graph.xml startDestination="@id/homeFragment"
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}