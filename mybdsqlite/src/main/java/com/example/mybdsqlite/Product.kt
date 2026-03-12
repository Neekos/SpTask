package com.example.mybdsqlite

data class Product(
   var id: Int = 0,
   var name: String = "",
   var image: String = "",
   var price: Double = 0.0,
   var categoryId: Int = 0,
   var categoryName: String = "" // Добавляем для отображения
)