package com.example.spprojectsqlitetask


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.spprojectsqlitetask.adapters.TaskAdapter
import com.example.spprojectsqlitetask.data.AppDatabase
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.databinding.ActivityMainBinding
import com.example.spprojectsqlitetask.dialogs.TaskDialog
import com.example.spprojectsqlitetask.fragments.TaskDetailFragment
import com.example.spprojectsqlitetask.notification.NotificationHelper
import com.example.spprojectsqlitetask.notification.TaskNotificationWorker
import com.example.spprojectsqlitetask.repository.TaskRepository
import com.example.spprojectsqlitetask.viewmodel.TasksViewModel
import com.example.spprojectsqlitetask.viewmodel.TasksViewModelFactory
import com.example.spprojectsqlitetask.widgets.TasksWidgetProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * MainActivity - Главный экран приложения "Список задач"
 *
 * Отвечает за:
 * 1. Отображение списка задач с помощью RecyclerView
 * 2. Фильтрацию задач по статусу (Все/Активные/Выполненные)
 * 3. Сортировку задач по сложности
 * 4. Отображение статистики (активные, выполненные, прогресс)
 * 5. Навигацию к деталям задачи
 * 6. Управление уведомлениями
 * 7. Интеграцию с виджетом
 *
 * Использует современные компоненты Android:
 * - ViewBinding для безопасной работы с View
 * - ViewModel для управления данными
 * - LiveData для автоматического обновления UI
 * - Coroutines для асинхронных операций
 * - WorkManager для фоновых задач
 * новое изменение !
 */
class MainActivity : AppCompatActivity() {

    // ============ VIEW BINDING ============
    /**
     * binding - экземпляр сгенерированного класса ActivityMainBinding
     * Содержит ссылки на все View из activity_main.xml
     * Заменяет findViewById() и обеспечивает типобезопасность
     */
    private lateinit var binding: ActivityMainBinding

    // ============ КОМПОНЕНТЫ ============
    private lateinit var database: AppDatabase      // База данных Room
    private lateinit var repository: TaskRepository // Репозиторий для работы с данными
    private lateinit var notificationHelper: NotificationHelper // Помощник уведомлений

    // ============ VIEW MODEL ============
    /**
     * viewModel - экземпляр TasksViewModel
     * by viewModels() - делегат для создания ViewModel с учетом жизненного цикла
     * TasksViewModelFactory - фабрика для передачи repository в ViewModel
     *
     * ViewModel сохраняется при повороте экрана
     */
    private val viewModel: TasksViewModel by viewModels {
        TasksViewModelFactory(repository)
    }

    // ============ АДАПТЕР ============
    private lateinit var adapter: TaskAdapter

    // ============ СОСТОЯНИЯ ============
    /**
     * enum для сортировки задач
     */
    private enum class SortOrder {
        DEFAULT,        // По умолчанию (по id)
        EASY_TO_HARD,   // От легкого к сложному
        HARD_TO_EASY    // От сложного к легкому
    }
    private var currentSortOrder = SortOrder.DEFAULT

    /**
     * Текущий фильтр по статусу
     * "all" - все задачи
     * "active" - активные
     * "completed" - выполненные
     */
    private var currentStatusFilter = "all"

    // ============ ЖИЗНЕННЫЙ ЦИКЛ ACTIVITY ============

    /**
     * onCreate - вызывается при создании Activity
     * Здесь происходит инициализация всех компонентов
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Включаем полноэкранный режим

        // Инициализация ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Последовательная инициализация компонентов
        setupWindowInsets()
        initializeComponents()
        setupViews()
        observeViewModel()
        setupListeners()
        loadInitialData()
        handleWidgetIntent(intent)
    }

    /**
     * setupWindowInsets - настройка отступов для системных панелей
     * Обрабатывает статус-бар и навигационную панель
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * initializeComponents - инициализация всех компонентов
     * База данных, репозиторий, уведомления
     */
    private fun initializeComponents() {
        database = AppDatabase.getDatabase(this)
        repository = TaskRepository(database.taskDao())
        notificationHelper = NotificationHelper(this)
        AppDatabase.insertSampleData(database) // Тестовые данные при первом запуске
    }

    /**
     * setupViews - настройка UI элементов
     */
    private fun setupViews() {
        updateCurrentDate()
        setupRecyclerView()
        updateFilterColors()
        updateSortButtonDisplay()
    }

    /**
     * updateCurrentDate - обновление отображения текущей даты
     * Формат: "Вт, 9 Дек, 2026г"
     */
    private fun updateCurrentDate() {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("E, d MMM, yyyy'г'", Locale("ru"))
        binding.titleTime.text = dateFormat.format(currentDate)
    }

    /**
     * setupRecyclerView - настройка списка задач
     * Создает адаптер с обработчиками событий
     */
    private fun setupRecyclerView() {
        binding.reclist.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(
            { task, isChecked ->              // Обработчик изменения чекбокса
                viewModel.updateTaskStatus(task.id, isChecked)
                updateWidget()
            },
            { task -> openTaskDetailFragment(task) },      // Клик на задачу
            { task -> showDeleteConfirmationDialog(task) } // Долгое нажатие
        )

        binding.reclist.adapter = adapter
    }

    // ============ НАБЛЮДЕНИЕ ЗА VIEWMODEL ============

    /**
     * observeViewModel - подписка на LiveData из ViewModel
     * UI автоматически обновляется при изменении данных
     */
    private fun observeViewModel() {
        // Отслеживаем отфильтрованные задачи для списка
        viewModel.filteredTasks.observe(this) { tasks ->
            adapter.submitList(tasks)              // Обновляем адаптер через DiffUtil
            updateEmptyListMessage(tasks.isEmpty()) // Показываем/скрываем сообщение
        }

        // Отслеживаем статистику
        viewModel.statistics.observe(this) { stats ->
            binding.active.text = stats.active.toString()
            binding.complete.text = stats.completed.toString()
            binding.porgress.text = String.format("%.1f%%", stats.progress)
        }

        // Отслеживаем ошибки
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    // ============ НАСТРОЙКА СЛУШАТЕЛЕЙ ============

    /**
     * setupListeners - установка обработчиков для UI элементов
     */
    private fun setupListeners() {
        // Фильтр "Все"
        binding.allList.setOnClickListener {
            currentStatusFilter = "all"
            viewModel.setStatusFilter("all")
            updateFilterColors()
        }

        // Фильтр "Активные"
        binding.activeList.setOnClickListener {
            currentStatusFilter = "active"
            viewModel.setStatusFilter("active")
            updateFilterColors()
        }

        // Фильтр "Выполненные"
        binding.completeList.setOnClickListener {
            currentStatusFilter = "completed"
            viewModel.setStatusFilter("completed")
            updateFilterColors()
        }

        // Кнопка сортировки
        binding.btnSort.setOnClickListener {
            // Циклическое переключение состояний сортировки
            currentSortOrder = when (currentSortOrder) {
                SortOrder.DEFAULT -> SortOrder.EASY_TO_HARD
                SortOrder.EASY_TO_HARD -> SortOrder.HARD_TO_EASY
                SortOrder.HARD_TO_EASY -> SortOrder.DEFAULT
            }
            // Передаем в ViewModel
            viewModel.setSortOrder(
                when (currentSortOrder) {
                    SortOrder.DEFAULT -> TasksViewModel.SortOrder.DEFAULT
                    SortOrder.EASY_TO_HARD -> TasksViewModel.SortOrder.EASY_TO_HARD
                    SortOrder.HARD_TO_EASY -> TasksViewModel.SortOrder.HARD_TO_EASY
                }
            )
            updateSortButtonDisplay()
        }

        // Кнопка добавления задачи
        binding.fabAdd.setOnClickListener { showTaskDialog() }
    }

    /**
     * loadInitialData - загрузка начальных данных
     * Запрос разрешений, проверка задач, планирование уведомлений
     */
    private fun loadInitialData() {
        requestNotificationPermission()
        lifecycleScope.launch {
            delay(500) // Ждем загрузки данных
            viewModel.allTasks.value?.let { checkIncompleteTasks(it) }
        }
        scheduleDailyReminders()
    }

    // ============ UI ОБНОВЛЕНИЯ ============

    /**
     * updateFilterColors - обновление цветов кнопок фильтрации
     * Подсвечивает выбранный фильтр синим цветом
     */
    private fun updateFilterColors() {
        // Сбрасываем все карточки
        resetFilterCard(binding.allList, binding.tvAllText)
        resetFilterCard(binding.activeList, binding.tvActiveText)
        resetFilterCard(binding.completeList, binding.tvCompletedText)

        // Подсвечиваем выбранную
        when (currentStatusFilter) {
            "all" -> setActiveFilterCard(binding.allList, binding.tvAllText)
            "active" -> setActiveFilterCard(binding.activeList, binding.tvActiveText)
            "completed" -> setActiveFilterCard(binding.completeList, binding.tvCompletedText)
        }
    }

    /**
     * resetFilterCard - сброс карточки фильтра в неактивное состояние
     */
    private fun resetFilterCard(card: CardView, textView: TextView) {
        card.setCardBackgroundColor(getColor(android.R.color.white))
        textView.setTextColor(getColor(android.R.color.black))
    }

    /**
     * setActiveFilterCard - установка активного состояния карточки
     */
    private fun setActiveFilterCard(card: CardView, textView: TextView) {
        card.setCardBackgroundColor(getColor(R.color.blue))
        textView.setTextColor(getColor(android.R.color.white))
    }

    /**
     * updateSortButtonDisplay - обновление отображения кнопки сортировки
     * Поворачивает иконку для визуальной обратной связи
     */
    private fun updateSortButtonDisplay() {
        when (currentSortOrder) {
            SortOrder.DEFAULT -> {
                binding.ivSortIcon.setImageResource(R.drawable.ic_sort)
                binding.ivSortIcon.rotation = 0f
            }
            SortOrder.EASY_TO_HARD -> {
                binding.ivSortIcon.setImageResource(R.drawable.ic_sort)
                binding.ivSortIcon.rotation = 0f
            }
            SortOrder.HARD_TO_EASY -> {
                binding.ivSortIcon.setImageResource(R.drawable.ic_sort)
                binding.ivSortIcon.rotation = 180f
            }
        }
    }

    /**
     * updateEmptyListMessage - показ сообщения о пустом списке
     */
    private fun updateEmptyListMessage(isEmpty: Boolean) {
        if (isEmpty) {
            binding.reclist.visibility = View.GONE
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.tvEmptyList.text = when (currentStatusFilter) {
                "active" -> "Нет активных задач"
                "completed" -> "Нет выполненных задач"
                else -> "Нет задач"
            }
        } else {
            binding.reclist.visibility = View.VISIBLE
            binding.tvEmptyList.visibility = View.GONE
        }
    }

    // ============ НАВИГАЦИЯ ============

    /**
     * openTaskDetailFragment - открытие фрагмента с деталями задачи
     * @param task выбранная задача
     */
    private fun openTaskDetailFragment(task: Task) {
        val fragment = TaskDetailFragment.newInstance(task, repository) { }
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null) // Добавляем в back stack для возврата
            .commit()
    }

    // ============ ДИАЛОГИ ============

    /**
     * showTaskDialog - показ диалога создания/редактирования задачи
     * @param task если null - создание, иначе - редактирование
     */
    private fun showTaskDialog(task: Task? = null) {
        TaskDialog(this, task) { newTask ->
            if (task == null) {
                viewModel.addTask(newTask)
                Toast.makeText(this, "✅ Задача добавлена", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.updateTask(newTask)
                Toast.makeText(this, "✅ Задача обновлена", Toast.LENGTH_SHORT).show()
            }
            updateWidget()
        }.show()
    }

    /**
     * showDeleteConfirmationDialog - диалог подтверждения удаления
     * @param task задача для удаления
     */
    private fun showDeleteConfirmationDialog(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Удаление задачи")
            .setMessage("Удалить \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteTask(task)
                Toast.makeText(this, "🗑 Задача удалена", Toast.LENGTH_SHORT).show()
                updateWidget()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // ============ WIDGET (ВИДЖЕТ) ============

    /**
     * updateWidget - обновление виджета на главном экране
     * Вызывается после любых изменений задач
     */
    private fun updateWidget() {
        TasksWidgetProvider.updateAllWidgets(this)
    }

    /**
     * handleWidgetIntent - обработка открытия приложения из виджета
     * @param intent Intent с данными задачи
     */
    private fun handleWidgetIntent(intent: Intent) {
        val taskId = intent.getIntExtra("open_task_id", -1)
        if (taskId != -1) {
            val task = Task(
                taskId,
                intent.getStringExtra("open_task_title") ?: "",
                intent.getStringExtra("open_task_description") ?: "",
                intent.getStringExtra("open_task_level") ?: "EASY",
                intent.getBooleanExtra("open_task_is_complete", false)
            )
            lifecycleScope.launch {
                delay(300) // Небольшая задержка для загрузки
                openTaskDetailFragment(task)
            }
        }
    }

    // ============ УВЕДОМЛЕНИЯ ============

    /**
     * requestNotificationPermission - запрос разрешения на уведомления
     * Требуется для Android 13 (API 33) и выше
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    /**
     * checkIncompleteTasks - проверка невыполненных задач
     * Показывает Snackbar и системное уведомление
     */
    private fun checkIncompleteTasks(tasks: List<Task>) {
        val incompleteCount = tasks.count { !it.isComplete }
        if (incompleteCount > 0) {
            Snackbar.make(binding.main, "📋 У вас $incompleteCount невыполненных задач", Snackbar.LENGTH_LONG)
                .setAction("Посмотреть") {
                    viewModel.setStatusFilter("active")
                    currentStatusFilter = "active"
                    updateFilterColors()
                }
                .setBackgroundTint(getColor(R.color.blue))
                .setTextColor(getColor(R.color.white))
                .show()

            notificationHelper.showDailyTaskReminder(incompleteCount)
        }
    }

    /**
     * scheduleDailyReminders - планирование ежедневных уведомлений
     * Использует WorkManager для надежного выполнения
     * Уведомление приходит в 9:00 утра каждый день
     */
    private fun scheduleDailyReminders() {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 9)  // 9 утра
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            // Если уже прошло 9:00, планируем на завтра
            if (timeInMillis < System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val workRequest = PeriodicWorkRequestBuilder<TaskNotificationWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calendar.timeInMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_task_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}