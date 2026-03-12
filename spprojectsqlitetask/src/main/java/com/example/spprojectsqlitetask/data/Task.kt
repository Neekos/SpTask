package com.example.spprojectsqlitetask.data

// Импортируем аннотации Room для работы с базой данных
import androidx.room.ColumnInfo  // Позволяет задать имя колонки в БД
import androidx.room.Entity       // Помечает класс как таблицу в БД
import androidx.room.PrimaryKey   // Указывает первичный ключ
import java.io.Serializable       // Позволяет передавать объект между компонентами (например, в Fragment)

/**
 * Модель данных "Задача"
 *
 * @Entity - аннотация Room, указывающая, что этот класс является таблицей в базе данных
 * tableName = "tasks" - имя таблицы в БД будет "tasks"
 *
 * Класс реализует Serializable, чтобы объекты Task можно было передавать через Bundle
 * (например, из Activity в Fragment)
 */
@Entity(tableName = "tasks")
data class Task(
    /**
     * @PrimaryKey - указывает, что это поле является первичным ключом
     * autoGenerate = true - значение будет генерироваться автоматически (автоинкремент)
     *
     * @ColumnInfo(name = "id") - явно указываем имя колонки в БД
     * val id: Int = 0 - поле неизменяемое (val), значение по умолчанию 0
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    /**
     * Заголовок задачи
     * @ColumnInfo(name = "title") - колонка будет называться "title"
     * По умолчанию пустая строка (без задачи не бывает, но валидация на уровне UI)
     */
    @ColumnInfo(name = "title")
    val title: String = "",

    /**
     * Описание задачи
     * @ColumnInfo(name = "description") - колонка называется "description"
     * В БД используется именно "description", а не "desc" (чтобы избежать зарезервированных слов)
     */
    @ColumnInfo(name = "description")
    val description: String = "",

    /**
     * Уровень сложности
     * @ColumnInfo(name = "level") - колонка "level"
     * Хранится как строка: "EASY", "MEDIUM" или "HARD"
     * По умолчанию "EASY"
     */
    @ColumnInfo(name = "level")
    val level: String = "EASY",

    /**
     * Статус выполнения задачи
     * @ColumnInfo(name = "is_complete") - колонка "is_complete" (соответствует старой БД)
     * Boolean значение: false - не выполнено, true - выполнено
     * По умолчанию false
     */
    @ColumnInfo(name = "is_complete")
    val isComplete: Boolean = false,

    /**
     * Дата создания/выполнения задачи
     * @ColumnInfo(name = "date") - колонка "date"
     * Хранится как Long (миллисекунды с 1970 года)
     * По умолчанию текущее время системы
     */
    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis()
) : Serializable {

    /**
     * Вычисляемое свойство для обратной совместимости
     *
     * В старом коде использовалось поле "desc", теперь оно называется "description"
     * Это свойство позволяет старому коду продолжать работать:
     * task.desc будет возвращать значение task.description
     *
     * @return значение поля description
     */
    val desc: String
        get() = description

    /**
     * Вычисляемое свойство для удобной работы с enum
     *
     * В БД уровень сложности хранится как строка ("EASY", "MEDIUM", "HARD")
     * Но в коде удобнее работать с enum LEVEL
     * Это свойство конвертирует строку в enum при каждом обращении
     *
     * @return enum LEVEL соответствующий строке level
     * @throws IllegalArgumentException если строка не соответствует ни одному enum
     */
    val levels: LEVEL
        get() = LEVEL.valueOf(level)
}

/**
 * Перечисление уровней сложности задачи
 *
 * implements Serializable - чтобы можно было передавать через Bundle
 * Используется в UI для отображения и в логике для сортировки/фильтрации
 */
enum class LEVEL : Serializable {
    EASY,    // Легкая задача (зеленый цвет в UI)
    MEDIUM,  // Средняя сложность (оранжевый цвет)
    HARD     // Сложная задача (красный цвет)
}