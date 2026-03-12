package com.example.spprojectsqlitetask.dialogs

import android.animation.ObjectAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.spprojectsqlitetask.R
import com.example.spprojectsqlitetask.data.LEVEL
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.databinding.DialogTaskBinding

/**
 * TaskDialog - диалог для создания или редактирования задачи
 *
 * Наследуется от Dialog, отображает форму с полями:
 * - Название задачи (обязательное)
 * - Описание задачи (необязательное)
 * - Выбор сложности (EASY/MEDIUM/HARD)
 *
 * Использует ViewBinding для безопасной работы с View
 *
 * @param context - Activity, в которой открывается диалог
 * @param task - существующая задача (null для создания новой)
 * @param onSave - колбэк, вызываемый при сохранении задачи
 */
class TaskDialog(
    private val context: AppCompatActivity,
    private var task: Task? = null,
    private val onSave: (Task) -> Unit
) : Dialog(context) {

    // ============ VIEW BINDING ============
    private lateinit var binding: DialogTaskBinding

    // ============ СОСТОЯНИЯ ============
    private var selectedLevel: LEVEL = task?.levels ?: LEVEL.EASY  // Выбранный уровень сложности
    private var isEditMode = task != null                          // Режим редактирования (true - редактирование, false - создание)

    // ============ ЖИЗНЕННЫЙ ЦИКЛ ДИАЛОГА ============

    /**
     * onCreate - вызывается при создании диалога
     * Здесь происходит инициализация всех компонентов
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ПРОВЕРКА: если активность уничтожена, не создаем диалог
        // Это предотвращает вылеты при попытке показать диалог на закрытой Activity
        if (context.isFinishing || context.isDestroyed) {
            dismiss()
            return
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Инициализация ViewBinding
        binding = DialogTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка размеров окна (на всю ширину экрана)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupViews()
        setupListeners()
        setupLevelSelection()

        // Если это режим редактирования - загружаем данные задачи
        if (isEditMode) {
            loadTaskData()
        }
    }

    // ============ ИНИЦИАЛИЗАЦИЯ UI ============

    /**
     * setupViews - начальная настройка UI элементов
     * Устанавливает заголовок диалога в зависимости от режима
     */
    private fun setupViews() {
        binding.dialogTitle.text = if (isEditMode) "Редактировать задачу" else "Новая задача"
    }

    // ============ НАСТРОЙКА СЛУШАТЕЛЕЙ ============

    /**
     * setupListeners - настройка обработчиков для кнопок
     */
    private fun setupListeners() {
        // Кнопка "Отмена" - просто закрываем диалог
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        // Кнопка "Сохранить" - сохраняем задачу
        binding.btnSave.setOnClickListener {
            saveTask()
        }
    }

    /**
     * setupLevelSelection - настройка выбора уровня сложности
     * Устанавливает начальное выделение и обработчики для каждой карточки
     */
    private fun setupLevelSelection() {
        // ПРОВЕРКА: если активность уничтожена, не настраиваем слушатели
        if (context.isFinishing || context.isDestroyed) {
            return
        }

        // Устанавливаем начальный выбор (по умолчанию EASY или сохраненный уровень)
        updateLevelSelection()

        // Обработчик для карточки EASY
        binding.cardEasy.setOnClickListener {
            if (!isValidContext()) return@setOnClickListener
            selectedLevel = LEVEL.EASY
            updateLevelSelection()
            animateCard(binding.cardEasy)
        }

        // Обработчик для карточки MEDIUM
        binding.cardMedium.setOnClickListener {
            if (!isValidContext()) return@setOnClickListener
            selectedLevel = LEVEL.MEDIUM
            updateLevelSelection()
            animateCard(binding.cardMedium)
        }

        // Обработчик для карточки HARD
        binding.cardHard.setOnClickListener {
            if (!isValidContext()) return@setOnClickListener
            selectedLevel = LEVEL.HARD
            updateLevelSelection()
            animateCard(binding.cardHard)
        }
    }

    // ============ АНИМАЦИЯ ============

    /**
     * animateCard - анимация нажатия на карточку
     * Создает эффект "сжатия" карточки при клике
     *
     * @param card - карточка для анимации (CardView)
     */
    private fun animateCard(card: CardView) {
        if (!isValidContext()) return

        try {
            // Анимация сжатия по оси X (горизонтальное сжатие)
            ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f, 1f).apply {
                duration = 200  // ИСПРАВЛЕНО: было ObjectAnimator.setDuration, стало duration
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            // Анимация сжатия по оси Y (вертикальное сжатие)
            ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f, 1f).apply {
                duration = 200  // ИСПРАВЛЕНО
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============ ОБНОВЛЕНИЕ ВЫДЕЛЕНИЯ ============

    /**
     * updateLevelSelection - обновление визуального выделения выбранного уровня
     * Подсвечивает выбранную карточку и сбрасывает остальные
     */
    private fun updateLevelSelection() {
        if (!isValidContext()) return

        try {
            // Сбрасываем стиль всех карточек
            resetLevelCards()

            // Подсвечиваем выбранную карточку в зависимости от уровня
            when (selectedLevel) {
                LEVEL.EASY -> {
                    binding.cardEasy.setCardBackgroundColor(context.getColor(R.color.green))
                    binding.tvEasy.setTextColor(context.getColor(R.color.white))
                    binding.cardEasy.radius = 20f           // Скругление углов
                    binding.cardEasy.cardElevation = 8f     // Увеличиваем тень для выделения
                    binding.tvEasy.textSize = 18f           // Увеличиваем текст
                }
                LEVEL.MEDIUM -> {
                    binding.cardMedium.setCardBackgroundColor(context.getColor(R.color.orange))
                    binding.tvMedium.setTextColor(context.getColor(R.color.white))
                    binding.cardMedium.radius = 20f
                    binding.cardMedium.cardElevation = 8f
                    binding.tvMedium.textSize = 18f
                }
                LEVEL.HARD -> {
                    binding.cardHard.setCardBackgroundColor(context.getColor(R.color.red))
                    binding.tvHard.setTextColor(context.getColor(R.color.white))
                    binding.cardHard.radius = 20f
                    binding.cardHard.cardElevation = 8f
                    binding.tvHard.textSize = 18f
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * resetLevelCards - сброс всех карточек в исходное состояние
     * Убирает выделение со всех карточек
     */
    private fun resetLevelCards() {
        if (!isValidContext()) return

        try {
            // Сброс фона на светлые версии цветов
            binding.cardEasy.setCardBackgroundColor(context.getColor(R.color.light_green))
            binding.cardMedium.setCardBackgroundColor(context.getColor(R.color.light_orange))
            binding.cardHard.setCardBackgroundColor(context.getColor(R.color.light_red))

            // Сброс цвета текста на белый
            binding.tvEasy.setTextColor(context.getColor(R.color.white))
            binding.tvMedium.setTextColor(context.getColor(R.color.white))
            binding.tvHard.setTextColor(context.getColor(R.color.white))

            // Сброс размера текста на стандартный
            binding.tvEasy.textSize = 16f
            binding.tvMedium.textSize = 16f
            binding.tvHard.textSize = 16f

            // Сброс тени на стандартную
            binding.cardEasy.cardElevation = 2f
            binding.cardMedium.cardElevation = 2f
            binding.cardHard.cardElevation = 2f
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============ ЗАГРУЗКА ДАННЫХ ============

    /**
     * loadTaskData - загрузка данных существующей задачи в поля формы
     * Вызывается только в режиме редактирования
     */
    private fun loadTaskData() {
        if (!isValidContext()) return

        task?.let {
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)
            selectedLevel = it.levels
            updateLevelSelection()
        }
    }

    // ============ СОХРАНЕНИЕ ЗАДАЧИ ============

    /**
     * saveTask - сохранение задачи (создание новой или обновление существующей)
     *
     * Логика:
     * 1. Проверяем валидность контекста
     * 2. Получаем данные из полей ввода
     * 3. Валидируем название (не пустое)
     * 4. Создаем объект Task
     * 5. Вызываем колбэк onSave
     * 6. Закрываем диалог
     */
    private fun saveTask() {
        // ПРОВЕРКА: если активность уничтожена, не сохраняем
        if (!isValidContext()) {
            dismiss()
            return
        }

        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // Валидация: название обязательно для заполнения
        if (title.isEmpty()) {
            binding.etTitle.error = "Введите название задачи"
            return
        }

        // Создаем объект задачи
        val newTask = if (isEditMode) {
            // Для редактирования - копируем существующую задачу с новыми значениями
            task?.copy(
                title = title,
                description = description,
                level = selectedLevel.name  // Конвертируем enum в строку "EASY"/"MEDIUM"/"HARD"
            ) ?: Task(
                title = title,
                description = description,
                level = selectedLevel.name
            )
        } else {
            // Для новой задачи (id будет сгенерирован автоматически в БД)
            Task(
                title = title,
                description = description,
                level = selectedLevel.name
            )
        }

        try {
            onSave(newTask) // Вызываем колбэк с созданной задачей
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dismiss() // Закрываем диалог
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    /**
     * isValidContext - проверка, что контекст еще валиден для операций с UI
     *
     * @return true если контекст активен и можно работать с UI
     *
     * Эта проверка предотвращает вылеты при попытке обновить UI
     * после того, как Activity уже была уничтожена
     */
    private fun isValidContext(): Boolean {
        return !context.isFinishing && !context.isDestroyed && context.window != null
    }

    /**
     * onStop - вызывается при остановке диалога
     * Можно добавить очистку ресурсов при необходимости
     */
    override fun onStop() {
        super.onStop()
        // Здесь можно освободить ресурсы, если нужно
    }
}