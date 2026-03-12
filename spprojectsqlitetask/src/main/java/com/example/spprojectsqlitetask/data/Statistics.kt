package com.example.spprojectsqlitetask.data

data class Statistics(
    val active: Int,      // Количество активных задач
    val completed: Int,   // Количество выполненных задач
    val progress: Float   // Процент выполнения (0-100)
)