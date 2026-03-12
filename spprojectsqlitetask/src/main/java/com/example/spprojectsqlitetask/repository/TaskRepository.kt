package com.example.spprojectsqlitetask.repository

// Импорты классов из data слоя
import com.example.spprojectsqlitetask.data.Statistics
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.data.TaskDao
// Импорты для работы с корутинами и Flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
// Импорт для работы с датами
import java.util.Calendar

/**
 * TaskRepository - Репозиторий для работы с задачами
 *
 * Репозиторий (Repository) - это паттерн проектирования, который:
 * 1. Инкапсулирует логику работы с данными
 * 2. Предоставляет чистый API для Activity/Fragment
 * 3. Скрывает источник данных (БД, сеть, кэш)
 * 4. Централизует изменения данных
 *
 * @param taskDao - Data Access Object для работы с таблицей tasks
 * Репозиторий не знает о Room, он работает только с TaskDao
 */
class TaskRepository(private val taskDao: TaskDao) {

    // ============ FLOW (автоматическое обновление) ============

    /**
     * allTasks - Flow всех задач с автоматическим обновлением
     *
     * Когда данные в БД меняются, Flow автоматически отправляет
     * новые списки всем подписчикам (например, Activity)
     * Идеально для RecyclerView с автообновлением
     */
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    /**
     * activeTasks - Flow только активных задач
     * Использует getActiveTasksFlow() из TaskDao
     */
    val activeTasks: Flow<List<Task>> = taskDao.getActiveTasksFlow()

    /**
     * completedTasks - Flow выполненных задач
     */
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasksFlow()

    /**
     * getTasksByLevelFlow - Flow задач по уровню сложности
     *
     * @param level "EASY", "MEDIUM" или "HARD"
     * @return Flow<List<Task>> с автоматическим обновлением
     */
    fun getTasksByLevelFlow(level: String): Flow<List<Task>> =
        taskDao.getTasksByLevelFlow(level)

    // ============ CREATE (Создание) ============

    /**
     * addTask - добавление новой задачи
     *
     * @param task объект Task для добавления
     * @return Long - id созданной задачи
     *
     * suspend - функция может приостанавливаться (корутина)
     * Должна вызываться из lifecycleScope.launch { ... }
     */
    suspend fun addTask(task: Task): Long {
        return taskDao.insertTask(task)  // Пробрасываем вызов в DAO
    }

    // ============ READ (Чтение) - однократные запросы ============


    /**
     * getAllTasksOnce - получить все задачи один раз
     *
     * Использует .first() на Flow, чтобы получить текущее значение
     * без подписки на дальнейшие обновления
     *
     * @return List<Task> - текущий список всех задач
     */
    suspend fun getAllTasksOnce(): List<Task> {
        return taskDao.getAllTasks().first()  // first() - suspend функция
    }

    /**
     * getActiveTasksOnce - активные задачи (однократно)
     */
    suspend fun getActiveTasksOnce(): List<Task> {
        return taskDao.getActiveTasks()
    }

    /**
     * getCompletedTasksOnce - выполненные задачи (однократно)
     */
    suspend fun getCompletedTasksOnce(): List<Task> {
        return taskDao.getCompletedTasks()
    }

    /**
     * getTaskById - получение задачи по ID
     *
     * @param taskId идентификатор задачи
     * @return Task? - null если задача не найдена
     */
    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * getTasksByLevel - задачи по уровню сложности
     *
     * @param level "EASY", "MEDIUM", "HARD"
     */
    suspend fun getTasksByLevel(level: String): List<Task> {
        return taskDao.getTasksByLevel(level)
    }

    /**
     * getTasksByDate - задачи за конкретный день
     *
     * Сложная логика: вычисляет начало и конец дня по переданной дате
     *
     * @param date дата в миллисекундах
     * @return List<Task> задачи за этот день
     */
    suspend fun getTasksByDate(date: Long): List<Task> {
        // Calendar - утилита для работы с датами
        val calendar = Calendar.getInstance().apply {
            timeInMillis = date                     // Устанавливаем переданную дату
            set(Calendar.HOUR_OF_DAY, 0)            // 0 часов
            set(Calendar.MINUTE, 0)                  // 0 минут
            set(Calendar.SECOND, 0)                  // 0 секунд
            set(Calendar.MILLISECOND, 0)             // 0 миллисекунд
        }
        val startOfDay = calendar.timeInMillis      // Начало дня

        calendar.add(Calendar.DAY_OF_MONTH, 1)      // Добавляем 1 день
        val endOfDay = calendar.timeInMillis        // Конец дня (начало следующего)

        // Запрос в БД: date BETWEEN startOfDay AND endOfDay
        return taskDao.getTasksByDate(startOfDay, endOfDay)
    }

    // ============ UPDATE (Обновление) ============

    /**
     * updateTask - полное обновление задачи
     *
     * @param task задача с обновленными данными
     * @return Int - количество обновленных строк (1 если успешно)
     */
    suspend fun updateTask(task: Task): Int {
        return taskDao.updateTask(task)
    }

    /**
     * updateTaskStatus - быстрое обновление только статуса
     *
     * @param taskId идентификатор задачи
     * @param isComplete новый статус
     */
    suspend fun updateTaskStatus(taskId: Int, isComplete: Boolean): Int {
        return taskDao.updateTaskStatus(taskId, isComplete)
    }

    /**
     * updateTaskTitle - обновление заголовка
     */
    suspend fun updateTaskTitle(taskId: Int, newTitle: String): Int {
        return taskDao.updateTaskTitle(taskId, newTitle)
    }

    /**
     * updateTaskDescription - обновление описания
     */
    suspend fun updateTaskDescription(taskId: Int, newDesc: String): Int {
        return taskDao.updateTaskDescription(taskId, newDesc)
    }

    /**
     * updateTaskLevel - обновление уровня сложности
     */
    suspend fun updateTaskLevel(taskId: Int, newLevel: String): Int {
        return taskDao.updateTaskLevel(taskId, newLevel)
    }

    // ============ DELETE (Удаление) ============

    /**
     * deleteTask - удаление задачи по объекту
     */
    suspend fun deleteTask(task: Task): Int {
        return taskDao.deleteTask(task)
    }

    /**
     * deleteTaskById - удаление задачи по ID
     * (более эффективно, не требует создания объекта)
     */
    suspend fun deleteTaskById(taskId: Int): Int {
        return taskDao.deleteTaskById(taskId)
    }

    /**
     * deleteCompletedTasks - удаление всех выполненных задач
     * Полезно для функции "Очистить выполненные"
     */
    suspend fun deleteCompletedTasks(): Int {
        return taskDao.deleteCompletedTasks()
    }

    /**
     * deleteAllTasks - удаление ВСЕХ задач
     * Осторожно! Безвозвратно удаляет все данные
     */
    suspend fun deleteAllTasks(): Int {
        return taskDao.deleteAllTasks()
    }

    // ============ STATISTICS (Статистика) ============

    /**
     * getStatistics - получение статистики по задачам
     *
     * Вычисляет:
     * - total: общее количество задач
     * - completed: выполненные
     * - active: активные
     * - progress: процент выполнения
     *
     * @return Statistics объект со всей статистикой
     */
    suspend fun getStatistics(): Statistics {
        val total = taskDao.getTotalCount()           // Всего задач
        val completed = taskDao.getCompletedCount()   // Выполненных
        val active = taskDao.getActiveCount()         // Активных

        // Прогресс: (выполненные / всего) * 100
        val progress = if (total > 0) (completed.toFloat() / total * 100) else 0f

        return Statistics(active, completed, progress)
    }

    /**
     * getCountByLevel - количество задач по уровню сложности
     *
     * @param level "EASY", "MEDIUM", "HARD"
     */
    suspend fun getCountByLevel(level: String): Int {
        return taskDao.getCountByLevel(level)
    }

    // ============ SEARCH (Поиск) ============

    /**
     * searchTasks - поиск задач по тексту
     *
     * TODO: нужно реализовать метод в TaskDao
     * Например:
     * @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
     *
     * @param query текст для поиска
     * @return List<Task> найденные задачи (пока пустой список)
     */
    suspend fun searchTasks(query: String): List<Task> {
        // TODO: implement search in TaskDao
        return emptyList()
    }

    // ============ SORTING (Сортировка) ============

    /**
     * sortTasksByLevelEasyToHard - сортировка от легкого к сложному
     *
     * Использует sortedBy для создания нового отсортированного списка
     * Не изменяет оригинальный список
     *
     * @param tasks исходный список задач
     * @return новый отсортированный список
     */
    fun sortTasksByLevelEasyToHard(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { task ->
            when (task.level) {
                "EASY" -> 1      // Самый маленький номер - первые
                "MEDIUM" -> 2
                "HARD" -> 3
                else -> 4         // На всякий случай
            }
        }
    }

    /**
     * sortTasksByLevelHardToEasy - сортировка от сложного к легкому
     */
    fun sortTasksByLevelHardToEasy(tasks: List<Task>): List<Task> {
        return tasks.sortedByDescending { task ->
            when (task.level) {
                "EASY" -> 1
                "MEDIUM" -> 2
                "HARD" -> 3
                else -> 4
            }
        }
    }

    /**
     * sortTasksByDateNewest - сортировка по дате (новые сначала)
     */
    fun sortTasksByDateNewest(tasks: List<Task>): List<Task> {
        return tasks.sortedByDescending { it.date }  // Большая дата = новее
    }

    /**
     * sortTasksByDateOldest - сортировка по дате (старые сначала)
     */
    fun sortTasksByDateOldest(tasks: List<Task>): List<Task> {
        return tasks.sortedBy { it.date }            // Меньшая дата = старее
    }
}