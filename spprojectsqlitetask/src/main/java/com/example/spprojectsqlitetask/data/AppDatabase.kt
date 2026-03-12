package com.example.spprojectsqlitetask.data

// Импорты для работы с контекстом Android и Room
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
// Импорты для работы с корутинами (асинхронность)
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase - главный класс базы данных Room
 *
 * Room - это библиотека от Google для работы с SQLite, которая:
 * 1. Проверяет SQL запросы во время компиляции
 * 2. Минимизирует шаблонный код
 * 3. Поддерживает корутины и Flow
 * 4. Автоматически конвертирует SQL запросы в Kotlin объекты
 *
 * @Database - аннотация, указывающая Room на структуру БД
 * - entities: список таблиц (классов с @Entity)
 * - version: версия БД (увеличивать при изменениях структуры)
 * - exportSchema: экспортировать схему для миграций (false для разработки)
 */
@Database(
    entities = [Task::class],      // Единственная таблица - Task
    version = 2,                    // Текущая версия БД (была 1, стала 2)
    exportSchema = false            // Не экспортировать схему (для продакшена обычно true)
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Абстрактный метод для получения Data Access Object (DAO)
     * Room автоматически генерирует реализацию этого метода
     * Через taskDao() мы будем выполнять все операции с таблицей tasks
     */
    abstract fun taskDao(): TaskDao

    /**
     * Companion object - аналог static в Java
     * Содержит методы для получения экземпляра БД (синглтон)
     */
    companion object {
        /**
         * @Volatile - гарантирует, что изменения INSTANCE будут видны всем потокам
         * Это важно для правильной работы синглтона в многопоточной среде
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * getDatabase - получение экземпляра базы данных (паттерн Singleton)
         *
         * Синглтон гарантирует, что во всем приложении будет только одно
         * соединение с базой данных, что предотвращает утечки ресурсов
         *
         * @param context контекст приложения (используем applicationContext)
         * @return экземпляр AppDatabase
         */
        fun getDatabase(context: Context): AppDatabase {
            // Если INSTANCE не null - возвращаем его
            // Если null - создаем новый через synchronized
            return INSTANCE ?: synchronized(this) {
                // Создаем новый экземпляр БД
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // Контекст приложения (не Activity!)
                    AppDatabase::class.java,      // Класс базы данных
                    "task_database"               // Имя файла БД (task_database.db)
                )
                    /**
                     * fallbackToDestructiveMigration() - стратегия при изменении версии БД
                     *
                     * При увеличении version (например, с 1 до 2) Room:
                     * 1. Удаляет все существующие таблицы
                     * 2. Создает новые таблицы по новой структуре
                     * 3. Все старые данные теряются!
                     *
                     * Для продакшена нужно использовать миграции (.addMigrations())
                     * Для разработки .fallbackToDestructiveMigration() удобно
                     */
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        /**
         * insertSampleData - добавление тестовых данных при первом запуске
         *
         * Запускается в фоновом потоке (Dispatchers.IO) через корутину
         * Проверяет, пустая ли БД, и добавляет примеры задач
         *
         * @param database экземпляр базы данных
         */
        fun insertSampleData(database: AppDatabase) {
            // CoroutineScope(Dispatchers.IO) - создаем корутину в фоновом потоке
            // launch - запускаем асинхронную операцию
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.taskDao()  // Получаем DAO для работы с задачами

                // Проверяем, есть ли уже данные в БД
                // getTotalCount() - метод из TaskDao, возвращает количество записей
                if (dao.getTotalCount() == 0) {
                    // Создаем список тестовых задач
                    val sampleTasks = listOf(
                        Task(
                            title = "Сделать проект",
                            description = "Закончить задание по Android",
                            level = "MEDIUM",      // Средняя сложность
                            isComplete = false      // Не выполнена
                        ),
                        Task(
                            title = "Позвонить маме",
                            description = "Узнать как дела",
                            level = "EASY",         // Легкая задача
                            isComplete = true        // Выполнена
                        ),
                        Task(
                            title = "Сходить в спортзал",
                            description = "Тренировка спины",
                            level = "HARD",          // Сложная задача
                            isComplete = false        // Не выполнена
                        )
                    )

                    // Вставляем каждую задачу в БД
                    sampleTasks.forEach { task ->
                        dao.insertTask(task)  // insertTask - метод из TaskDao
                    }

                    // Логируем успешное добавление
                    println("✅ Тестовые данные добавлены в БД")
                }
            }
        }

        /**
         * insertAllTasks - вставка нескольких задач одной операцией
         *
         * Удобно для импорта задач из файла или синхронизации
         *
         * @param database экземпляр базы данных
         * @param tasks список задач для вставки
         */
        fun insertAllTasks(database: AppDatabase, tasks: List<Task>) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.taskDao()
                // Поочередно вставляем каждую задачу
                tasks.forEach { task ->
                    dao.insertTask(task)
                }
            }
        }

        /**
         * clearDatabase - полная очистка базы данных
         *
         * Удаляет все задачи из таблицы
         * Полезно для тестирования или сброса данных
         *
         * @param database экземпляр базы данных
         */
        fun clearDatabase(database: AppDatabase) {
            CoroutineScope(Dispatchers.IO).launch {
                val dao = database.taskDao()
                dao.deleteAllTasks()  // Метод из TaskDao, удаляет все записи
            }
        }
    }
}