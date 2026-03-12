package com.example.fragmentlesson1

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var btnPhotos: Button
    private lateinit var btnVideos: Button

    // Переменная для хранения текущего фрагмента
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация кнопок
        btnPhotos = findViewById(R.id.btn_photos)
        btnVideos = findViewById(R.id.btn_videos)

        // Установка начального фрагмента
        if (savedInstanceState == null) {
            loadFragment(PhotosFragment(), "photos")
            btnPhotos.isEnabled = false // Делаем кнопку неактивной
        }

        // Обработчики кликов по кнопкам
        btnPhotos.setOnClickListener {
            if (currentFragment !is PhotosFragment) {
                loadFragment(PhotosFragment(), "photos")
                updateButtonStates(true)
            }
        }

        btnVideos.setOnClickListener {
            if (currentFragment !is VideosFragment) {
                loadFragment(VideosFragment(), "videos")
                updateButtonStates(false)
            }
        }
    }

    /**
     * Загрузка фрагмента в контейнер
     * @param fragment - фрагмент для загрузки
     * @param tag - тег для идентификации
     */
    private fun loadFragment(fragment: Fragment, tag: String) {
        currentFragment = fragment

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    /**
     * Обновление состояния кнопок
     * @param isPhotosActive - активна ли кнопка фото
     */
    private fun updateButtonStates(isPhotosActive: Boolean) {
        if (isPhotosActive) {
            btnPhotos.isEnabled = false
            btnVideos.isEnabled = true

            btnPhotos.alpha = 0.5f
            btnVideos.alpha = 1f
        } else {
            btnPhotos.isEnabled = true
            btnVideos.isEnabled = false

            btnPhotos.alpha = 1f
            btnVideos.alpha = 0.5f
        }
    }

    /**
     * Обработка кнопки "Назад"
     */
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            updateCurrentFragment()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Обновление текущего фрагмента после навигации назад
     */
    private fun updateCurrentFragment() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        currentFragment = fragment

        when (fragment) {
            is PhotosFragment -> updateButtonStates(true)
            is VideosFragment -> updateButtonStates(false)
        }
    }
}