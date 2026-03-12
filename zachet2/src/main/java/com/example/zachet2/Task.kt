package com.example.zachet2

data class Task(
    val id: Int,
    var title: String,
    var isCompleted: Boolean = false
)