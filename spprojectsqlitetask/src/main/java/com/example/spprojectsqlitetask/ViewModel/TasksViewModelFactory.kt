package com.example.spprojectsqlitetask.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spprojectsqlitetask.repository.TaskRepository

/**
 * TasksViewModelFactory - Фабрика для создания ViewModel с параметрами
 *
 * ViewModelFactory - это паттерн проектирования, который решает проблему:
 * Как передать параметры в конструктор ViewModel?
 *
 * Стандартный ViewModelProvider может создать ViewModel только с пустым конструктором
 * Наша фабрика позволяет передать repository в TasksViewModel
 *
 * @param repository - репозиторий, который будет передан в ViewModel
 */
class TasksViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {

    /**
     * create - метод, который вызывается для создания экземпляра ViewModel
     *
     * @param modelClass - класс ViewModel, который нужно создать
     * @return T - созданный экземпляр ViewModel
     * @throws IllegalArgumentException - если запрошен неизвестный класс ViewModel
     *
     * Работа метода:
     * 1. Проверяем, что запрошенный класс совместим с TasksViewModel
     * 2. Если да - создаем TasksViewModel с нашим repository
     * 3. Если нет - выбрасываем исключение
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Проверяем, можно ли присвоить modelClass к TasksViewModel
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            // @Suppress("UNCHECKED_CAST") - подавляем предупреждение компилятора
            // Мы уверены в безопасности, так как проверили выше
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(repository) as T
        }
        // Если запросили другой тип ViewModel - ошибка
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}