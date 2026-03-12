package com.example.pz4nointernet


object ImageManager {

    // Список доступных изображений товаров
    val productImages = listOf(
        R.drawable.product_laptop,
        R.drawable.product_phone,
        R.drawable.product_headphones,
        R.drawable.product_monitor,
        R.drawable.product_keyboard,
        R.drawable.product_mouse,
        R.drawable.product_tablet,
        R.drawable.product_speaker
    )

    // Названия для изображений (для отображения в выборе)
    val imageNames = listOf(
        "Ноутбук",
        "Смартфон",
        "Наушники",
        "Монитор",
        "Клавиатура",
        "Мышь",
        "Планшет",
        "Колонки"
    )

    // Получить изображение по индексу
    fun getImageResId(index: Int): Int {
        return if (index in productImages.indices) {
            productImages[index]
        } else {
            R.drawable.placeholder_image
        }
    }

    // Получить название изображения по индексу
    fun getImageName(index: Int): String {
        return if (index in imageNames.indices) {
            imageNames[index]
        } else {
            "Изображение"
        }
    }

    // Получить случайное изображение
    fun getRandomImageResId(): Int {
        return productImages.random()
    }
}