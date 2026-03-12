package com.example.imagesmodul

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)

        val myImages: ImageView = findViewById(R.id.myImages2)
        myImages.clipToOutline = true

        // Создаем ImageView
        val imageView = ImageView(this).apply {
            // Устанавливаем изображение
            setImageResource(R.drawable.img)

            // Настройки масштабирования
            scaleType = ImageView.ScaleType.CENTER_CROP

            // Размеры
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300.dpToPx(this@MainActivity2) // расширение для конвертации dp в px
            )

            // Отступы
            setPadding(16.dpToPx(this@MainActivity2), 0, 16.dpToPx(this@MainActivity2), 0)
        }

        // Добавляем в layout
        val rootLayout = findViewById<LinearLayout>(R.id.root_layout)
        rootLayout.addView(imageView)
    }
    // Extension function для конвертации dp в px
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}