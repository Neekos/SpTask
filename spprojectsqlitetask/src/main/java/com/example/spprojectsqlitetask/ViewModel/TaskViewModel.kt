package com.example.spprojectsqlitetask.viewmodel

// Импорты компонентов Android Architecture Components
import androidx.lifecycle.LiveData          // Наблюдаемый источник данных
import androidx.lifecycle.MutableLiveData    // Изменяемая версия LiveData
import androidx.lifecycle.ViewModel          // Базовый класс для ViewModel
import androidx.lifecycle.viewModelScope     // CoroutineScope привязанный к ViewModel
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.data.LEVEL
import com.example.spprojectsqlitetask.repository.TaskRepository
import kotlinx.coroutines.flow.first         // Получение первого элемента из Flow
import kotlinx.coroutines.launch              // Запуск корутин

/**
 * TasksViewModel - отвечает за подготовку данных для UI
 *
 * ViewModel - это компонент архитектуры Android, который:
 * 1. Хранит данные, связанные с UI, и переживает поворот экрана
 * 2. Предоставляет данные через LiveData для автоматического обновления UI
 * 3. Содержит бизнес-логику по обработке данных
 * 4. Изолирует UI от источника данных (репозитория)
 *
 * @param repository - репозиторий для работы с задачами
 */
class TasksViewModel(private val repository: TaskRepository) : ViewModel() {

    // ============ ПОЛЯ ДАННЫХ ============

    /**
     * _allTasks - MutableLiveData (приватная изменяемая версия)
     * allTasks - LiveData (публичная неизменяемая версия для наблюдения)
     *
     * Принцип инкапсуляции: внешние классы могут только наблюдать,
     * но не изменять данные напрямую
     */
    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> = _allTasks

    /**
     * _filteredTasks - отфильтрованные задачи для отображения
     * filteredTasks - публичная версия для UI
     */
    private val _filteredTasks = MutableLiveData<List<Task>>()
    val filteredTasks: LiveData<List<Task>> = _filteredTasks

    /**
     * _isLoading - индикатор загрузки данных
     * Используется для показа ProgressBar в UI
     */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * _error - сообщения об ошибках
     * String? может быть null (нет ошибки)
     */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // ============ СОСТОЯНИЯ ФИЛЬТРОВ ============

    /**
     * Текущий фильтр по статусу
     * "all" - все задачи
     * "active" - активные (невыполненные)
     * "completed" - выполненные
     */
    private var currentStatusFilter = "all"

    /**
     * Текущий порядок сортировки
     * enum класс для типобезопасности
     */
    private var currentSortOrder = SortOrder.DEFAULT

    /**
     * Enum для сортировки задач по сложности
     */
    enum class SortOrder {
        DEFAULT,        // По умолчанию (по id)
        EASY_TO_HARD,   // От легкого к сложному
        HARD_TO_EASY    // От сложного к легкому
    }

    // ============ СТАТИСТИКА ============

    /**
     * _statistics - статистика по задачам
     */
    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    /**
     * Data class для хранения статистики
     * @param active - количество активных задач
     * @param completed - количество выполненных
     * @param progress - процент выполнения
     */
    data class Statistics(
        val active: Int,
        val completed: Int,
        val progress: Float
    )

    // ============ ИНИЦИАЛИЗАЦИЯ ============

    /**
     * init блок выполняется сразу после создания ViewModel
     * Здесь мы начинаем загрузку данных
     */
    init {
        loadTasks()
    }

    // ============ ОСНОВНЫЕ ОПЕРАЦИИ ============

    /**
     * loadTasks - загрузка всех задач из репозитория
     *
     * Использует viewModelScope.launch для асинхронной загрузки
     * viewModelScope автоматически отменяет корутины при уничтожении ViewModel
     *
     * Flow коллектор будет получать обновления при каждом изменении в БД
     */
    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true  // Показываем индикатор загрузки
            try {
                // Подписываемся на Flow из репозитория
                repository.allTasks.collect { tasks ->
                    _allTasks.value = tasks                    // Сохраняем все задачи
                    filterAndSortTasks(tasks)                  // Применяем фильтры
                    updateStatistics(tasks)                    // Обновляем статистику
                    _isLoading.value = false                   // Скрываем индикатор
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}" // Сообщаем об ошибке
                _isLoading.value = false
            }
        }
    }

    /**
     * addTask - добавление новой задачи
     * @param task - объект задачи для добавления
     */
    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.addTask(task)
                // Данные обновятся автоматически через Flow в loadTasks()
            } catch (e: Exception) {
                _error.value = "Ошибка добавления: ${e.message}"
            }
        }
    }

    /**
     * updateTask - обновление существующей задачи
     * @param task - задача с обновленными данными
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(task)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления: ${e.message}"
            }
        }
    }

    /**
     * updateTaskStatus - быстрое обновление только статуса задачи
     * @param taskId - идентификатор задачи
     * @param isComplete - новый статус
     */
    fun updateTaskStatus(taskId: Int, isComplete: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateTaskStatus(taskId, isComplete)
            } catch (e: Exception) {
                _error.value = "Ошибка изменения статуса: ${e.message}"
            }
        }
    }

    /**
     * deleteTask - удаление задачи
     * @param task - задача для удаления
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }

    // ============ ФИЛЬТРАЦИЯ И СОРТИРОВКА ============

    /**
     * setStatusFilter - установка фильтра по статусу
     * @param filter - "all", "active" или "completed"
     */
    fun setStatusFilter(filter: String) {
        currentStatusFilter = filter
        _allTasks.value?.let { filterAndSortTasks(it) } // Применяем фильтр к текущим данным
    }

    /**
     * setSortOrder - установка порядка сортировки
     * @param order - порядок сортировки из enum SortOrder
     */
    fun setSortOrder(order: SortOrder) {
        currentSortOrder = order
        _allTasks.value?.let { filterAndSortTasks(it) }
    }

    /**
     * filterAndSortTasks - применение фильтров и сортировки к списку задач
     * @param tasks - исходный список всех задач
     *
     * Логика:
     * 1. Сначала фильтруем по статусу
     * 2. Затем сортируем по сложности
     * 3. Результат сохраняем в _filteredTasks
     */
    private fun filterAndSortTasks(tasks: List<Task>) {
        // Шаг 1: Фильтрация по статусу
        val statusFiltered = when (currentStatusFilter) {
            "active" -> tasks.filter { !it.isComplete }      // Только невыполненные
            "completed" -> tasks.filter { it.isComplete }    // Только выполненные
            else -> tasks                                     // Все задачи
        }

        // Шаг 2: Сортировка по сложности
        val sorted = when (currentSortOrder) {
            SortOrder.DEFAULT -> statusFiltered              // Без сортировки (по id)

            SortOrder.EASY_TO_HARD -> statusFiltered.sortedBy { task ->
                when (task.levels) {
                    LEVEL.EASY -> 1      // EASY получает наименьший номер
                    LEVEL.MEDIUM -> 2    // MEDIUM средний
                    LEVEL.HARD -> 3      // HARD наибольший
                }
            }

            SortOrder.HARD_TO_EASY -> statusFiltered.sortedByDescending { task ->
                when (task.levels) {
                    LEVEL.EASY -> 1
                    LEVEL.MEDIUM -> 2
                    LEVEL.HARD -> 3
                }
            }
        }

        _filteredTasks.value = sorted // Обновляем LiveData
    }

    // ============ СТАТИСТИКА ============

    /**
     * updateStatistics - вычисление статистики по задачам
     * @param tasks - список всех задач
     *
     * Вычисляет:
     * - activeCount: количество активных задач
     * - completedCount: количество выполненных
     * - progress: процент выполнения
     */
    private fun updateStatistics(tasks: List<Task>) {
        val active = tasks.count { !it.isComplete }
        val completed = tasks.count { it.isComplete }
        val progress = if (tasks.isNotEmpty()) (completed.toFloat() / tasks.size * 100) else 0f
        _statistics.value = Statistics(active, completed, progress)
    }

    // ============ УПРАВЛЕНИЕ ОШИБКАМИ ============

    /**
     * clearError - очистка сообщения об ошибке
     * Вызывается после отображения ошибки в UI
     */
    fun clearError() {
        _error.value = null
    }
}