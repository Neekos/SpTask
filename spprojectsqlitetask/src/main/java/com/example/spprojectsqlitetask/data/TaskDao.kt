package com.example.spprojectsqlitetask.data

// Импортируем аннотации Room для работы с базой данных
import androidx.room.Dao      // Помечает интерфейс как Data Access Object
import androidx.room.Delete    // Аннотация для удаления
import androidx.room.Insert    // Аннотация для вставки
import androidx.room.Query     // Аннотация для кастомных SQL запросов
import androidx.room.Update    // Аннотация для обновления
import kotlinx.coroutines.flow.Flow  // Flow для асинхронного получения данных с автообновлением

/**
 * TaskDao (Data Access Object) - интерфейс для доступа к таблице tasks
 *
 * DAO - это паттерн проектирования, который:
 * 1. Инкапсулирует все операции с базой данных
 * 2. Предоставляет чистый API для работы с данными
 * 3. Скрывает детали реализации SQL запросов
 *
 * @Dao - аннотация Room, указывающая, что это интерфейс для работы с БД
 * Room автоматически генерирует реализацию этого интерфейса
 */
@Dao
interface TaskDao {

    // ============ CREATE (Создание) ============

    /**
     * Добавление новой задачи в таблицу
     *
     * @Insert - аннотация для вставки данных
     * Room сам генерирует SQL запрос INSERT INTO tasks VALUES(...)
     *
     * @param task объект Task для вставки
     * @return Long - id созданной записи (генерируется автоматически)
     *
     * suspend - функция может приостанавливаться (корутина)
     * Должна вызываться из корутины или другой suspend функции
     */
    @Insert
    suspend fun insertTask(task: Task): Long

    // ============ READ (Чтение) ============

    /**
     * Получение всех задач с автоматическим обновлением
     *
     * @Query - аннотация для кастомных SQL запросов
     * "SELECT * FROM tasks ORDER BY id DESC" - SQL запрос:
     * - SELECT * - выбрать все колонки
     * - FROM tasks - из таблицы tasks
     * - ORDER BY id DESC - сортировать по id в обратном порядке (новые сверху)
     *
     * @return Flow<List<Task>> - поток данных, который:
     * - Автоматически обновляется при изменениях в БД
     * - Подписчики получают новые данные автоматически
     * - Идеально для RecyclerView с автообновлением
     */
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Получение только активных (невыполненных) задач
     *
     * WHERE is_complete = 0 - условие: is_complete равно 0 (false)
     * Однократный запрос (не Flow), возвращает список сразу
     *
     * @return List<Task> - список активных задач
     */
    @Query("SELECT * FROM tasks WHERE is_complete = 0 ORDER BY id DESC")
    suspend fun getActiveTasks(): List<Task>

    /**
     * Получение только выполненных задач
     * WHERE is_complete = 1 - условие: is_complete равно 1 (true)
     */
    @Query("SELECT * FROM tasks WHERE is_complete = 1 ORDER BY id DESC")
    suspend fun getCompletedTasks(): List<Task>

    /**
     * Получение задачи по ID
     *
     * WHERE id = :taskId - параметризованный запрос
     * :taskId - значение подставляется из параметра функции
     *
     * @param taskId идентификатор задачи
     * @return Task? - может быть null, если задача не найдена
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    /**
     * Получение задач по уровню сложности
     *
     * WHERE level = :level - фильтр по уровню
     * @param level строка "EASY", "MEDIUM" или "HARD"
     */
    @Query("SELECT * FROM tasks WHERE level = :level ORDER BY id DESC")
    suspend fun getTasksByLevel(level: String): List<Task>

    /**
     * Получение задач за конкретный день
     *
     * WHERE date BETWEEN :startOfDay AND :endOfDay - диапазон дат
     * BETWEEN - оператор SQL для выбора значений в интервале
     *
     * @param startOfDay начало дня в миллисекундах
     * @param endOfDay конец дня в миллисекундах
     */
    @Query("SELECT * FROM tasks WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY id DESC")
    suspend fun getTasksByDate(startOfDay: Long, endOfDay: Long): List<Task>

    // ============ UPDATE (Обновление) ============

    /**
     * Полное обновление задачи
     *
     * @Update - аннотация для обновления
     * Room обновляет запись по id (должен быть в объекте)
     *
     * @param task объект Task с обновленными данными
     * @return Int - количество обновленных строк (должно быть 1)
     */
    @Update
    suspend fun updateTask(task: Task): Int

    /**
     * Быстрое обновление только статуса задачи
     *
     * Кастомный SQL запрос:
     * UPDATE tasks SET is_complete = :isComplete WHERE id = :taskId
     *
     * Более эффективно, чем полное обновление,
     * так как не нужно передавать все поля
     *
     * @param taskId идентификатор задачи
     * @param isComplete новый статус
     */
    @Query("UPDATE tasks SET is_complete = :isComplete WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Int, isComplete: Boolean): Int

    /**
     * Обновление заголовка задачи
     */
    @Query("UPDATE tasks SET title = :newTitle WHERE id = :taskId")
    suspend fun updateTaskTitle(taskId: Int, newTitle: String): Int

    /**
     * Обновление описания задачи
     */
    @Query("UPDATE tasks SET description = :newDesc WHERE id = :taskId")
    suspend fun updateTaskDescription(taskId: Int, newDesc: String): Int

    /**
     * Обновление уровня сложности
     */
    @Query("UPDATE tasks SET level = :newLevel WHERE id = :taskId")
    suspend fun updateTaskLevel(taskId: Int, newLevel: String): Int

    // ============ DELETE (Удаление) ============

    /**
     * Удаление задачи
     *
     * @Delete - аннотация для удаления
     * Room удаляет запись по id из объекта
     *
     * @param task объект Task для удаления
     * @return Int - количество удаленных строк
     */
    @Delete
    suspend fun deleteTask(task: Task): Int

    /**
     * Удаление задачи по ID (без создания объекта)
     *
     * Более эффективно, чем deleteTask, так как не нужно
     * создавать объект Task
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int): Int

    /**
     * Удаление всех выполненных задач
     * Полезно для функции "Очистить выполненные"
     */
    @Query("DELETE FROM tasks WHERE is_complete = 1")
    suspend fun deleteCompletedTasks(): Int

    /**
     * Удаление всех задач (очистка таблицы)
     * Осторожно! Безвозвратно удаляет все данные
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks(): Int

    // ============ STATISTICS (Статистика) ============

    /**
     * Получение общего количества задач
     *
     * SELECT COUNT(*) - функция подсчета строк
     * @return Int - количество записей
     */
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalCount(): Int

    /**
     * Количество выполненных задач
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_complete = 1")
    suspend fun getCompletedCount(): Int

    /**
     * Количество активных задач
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE is_complete = 0")
    suspend fun getActiveCount(): Int

    /**
     * Количество задач по уровню сложности
     *
     * @param level "EASY", "MEDIUM" или "HARD"
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE level = :level")
    suspend fun getCountByLevel(level: String): Int

    /**
     * Получение статистики по дням (для карусели дат)
     *
     * Сложный SQL запрос с группировкой:
     * - SELECT date, COUNT(*) as count - выбираем дату и считаем задачи
     * - FROM tasks - из таблицы tasks
     * - GROUP BY date - группируем по дате
     * - ORDER BY date DESC - сортируем по убыванию даты
     *
     * @return List<DateTaskCount> - список объектов с датой и количеством
     */
    @Query("""
        SELECT date, COUNT(*) as count 
        FROM tasks 
        GROUP BY date 
        ORDER BY date DESC
    """)
    suspend fun getTasksCountByDate(): List<DateTaskCount>

    // ============ FLOW ВЕРСИИ ДЛЯ АВТООБНОВЛЕНИЯ ============

    /**
     * Flow для активных задач с автообновлением
     *
     * В отличие от getActiveTasks(), этот метод возвращает Flow,
     * который будет автоматически обновлять подписчиков
     * при изменениях в БД
     */
    @Query("SELECT * FROM tasks WHERE is_complete = 0 ORDER BY id DESC")
    fun getActiveTasksFlow(): Flow<List<Task>>

    /**
     * Flow для выполненных задач
     */
    @Query("SELECT * FROM tasks WHERE is_complete = 1 ORDER BY id DESC")
    fun getCompletedTasksFlow(): Flow<List<Task>>

    /**
     * Flow для задач по уровню сложности
     *
     * @param level "EASY", "MEDIUM" или "HARD"
     */
    @Query("SELECT * FROM tasks WHERE level = :level ORDER BY id DESC")
    fun getTasksByLevelFlow(level: String): Flow<List<Task>>
}

/**
 * Вспомогательный класс для статистики по датам
 *
 * Используется только для возврата результата из getTasksCountByDate()
 * Не является Entity (не сохраняется в БД), просто контейнер для данных
 *
 * @param date дата в миллисекундах
 * @param count количество задач в этот день
 */
data class DateTaskCount(
    val date: Long,
    val count: Int
)