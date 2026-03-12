package com.example.spprojectsqlitetask.fragments

// ============ ИМПОРТЫ ============
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.spprojectsqlitetask.R
import com.example.spprojectsqlitetask.dialogs.TaskDialog
import com.example.spprojectsqlitetask.data.LEVEL
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.databinding.FragmentTaskDetailBinding
import com.example.spprojectsqlitetask.repository.TaskRepository
import kotlinx.coroutines.launch

/**
 * TaskDetailFragment - фрагмент для отображения детальной информации о задаче
 *
 * Отвечает за:
 * 1. Показ полной информации о задаче (заголовок, описание, сложность, статус)
 * 2. Редактирование задачи через диалог
 * 3. Навигацию назад к списку задач
 *
 * Использует ViewBinding для безопасной работы с View
 */
class TaskDetailFragment : Fragment() {

    // ============ VIEW BINDING ============

    /**
     * _binding - nullable переменная для хранения binding
     * Используется с паттерном для безопасного освобождения ресурсов
     */
    private var _binding: FragmentTaskDetailBinding? = null

    /**
     * binding - ненулевой геттер для доступа к binding
     * @throws IllegalStateException если binding не инициализирован
     */
    private val binding get() = _binding!!

    // ============ ДАННЫЕ ============

    /**
     * task - текущая отображаемая задача
     * Получается из arguments при создании фрагмента
     */
    private var task: Task? = null

    /**
     * onTaskUpdated - колбэк для уведомления родительской Activity
     * Вызывается после успешного редактирования задачи
     */
    private var onTaskUpdated: ((Task) -> Unit)? = null

    /**
     * repository - репозиторий для работы с базой данных
     * Передается из MainActivity при создании фрагмента
     */
    private var repository: TaskRepository? = null

    // ============ СОПУТСТВУЮЩИЙ ОБЪЕКТ ============

    companion object {
        /**
         * Ключ для передачи задачи через Bundle
         */
        private const val ARG_TASK = "task"

        /**
         * Фабричный метод для создания экземпляра фрагмента
         *
         * @param task задача для отображения
         * @param repository репозиторий для операций с БД
         * @param onTaskUpdated колбэк при обновлении задачи
         * @return новый экземпляр TaskDetailFragment
         *
         * Преимущества фабричного метода:
         * - Четкий интерфейс для передачи параметров
         * - Безопасная типизация
         * - Инкапсуляция создания фрагмента
         */
        fun newInstance(
            task: Task,
            repository: TaskRepository,
            onTaskUpdated: (Task) -> Unit
        ): TaskDetailFragment {
            val fragment = TaskDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_TASK, task)  // Task должен implements Serializable
            fragment.arguments = args
            fragment.onTaskUpdated = onTaskUpdated
            fragment.repository = repository
            return fragment
        }
    }

    // ============ ЖИЗНЕННЫЙ ЦИКЛ ФРАГМЕНТА ============

    /**
     * onCreateView - создание иерархии View для фрагмента
     *
     * @param inflater для раздувания layout
     * @param container родительский ViewGroup
     * @param savedInstanceState сохраненное состояние
     * @return корневой View фрагмента
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Инициализация ViewBinding
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * onViewCreated - вызывается после создания всех View
     * Здесь происходит инициализация данных и настройка слушателей
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем задачу из аргументов
        task = arguments?.getSerializable(ARG_TASK) as? Task

        // Проверка на null - обязательна для безопасной работы
        if (task == null) {
            Toast.makeText(context, "Ошибка загрузки задачи", Toast.LENGTH_SHORT).show()
            closeFragment()
            return
        }

        // Если задача загружена успешно - отображаем её
        task?.let { task ->
            updateTaskDisplay(task)

            // Обработчик кнопки "Назад"
            binding.btnDetailBack.setOnClickListener {
                closeFragment()
            }

            // Обработчик кнопки "Редактировать"
            binding.fabEdit.setOnClickListener {
                showEditDialog(task)
            }
        }
    }

    /**
     * onDestroyView - вызывается перед уничтожением View
     * Важно для очистки ссылок и предотвращения утечек памяти
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Освобождаем binding
    }

    // ============ НАВИГАЦИЯ ============

    /**
     * closeFragment - закрытие текущего фрагмента
     * Возвращает пользователя к списку задач
     */
    private fun closeFragment() {
        parentFragmentManager.popBackStack()
    }

    // ============ РЕДАКТИРОВАНИЕ ЗАДАЧИ ============

    /**
     * showEditDialog - отображение диалога редактирования задачи
     *
     * @param task задача для редактирования
     *
     * Последовательность действий:
     * 1. Проверяем Activity
     * 2. Создаем TaskDialog с текущей задачей
     * 3. После сохранения - обновляем задачу в БД
     * 4. Обновляем UI фрагмента
     * 5. Уведомляем родительскую Activity через колбэк
     */
    private fun showEditDialog(task: Task) {
        // Проверяем, что Activity существует и является AppCompatActivity
        val activity = requireActivity() as? AppCompatActivity
        if (activity == null) {
            Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show()
            return
        }

        // Создаем диалог с задачей и колбэком при сохранении
        TaskDialog(activity, task) { updatedTask ->
            // Запускаем корутину для асинхронного обновления
            lifecycleScope.launch {
                // Копируем задачу с сохранением ID
                val updatedTaskWithId = updatedTask.copy(id = task.id)

                // Обновляем в базе данных
                val rows = repository?.updateTask(updatedTaskWithId) ?: 0

                // Если обновление успешно (rows > 0)
                if (rows > 0) {
                    // Обновляем локальную переменную
                    this@TaskDetailFragment.task = updatedTaskWithId

                    // Обновляем UI
                    updateTaskDisplay(updatedTaskWithId)

                    // Уведомляем родительскую Activity
                    onTaskUpdated?.invoke(updatedTaskWithId)

                    // Показываем сообщение об успехе
                    Toast.makeText(context, "✅ Задача обновлена", Toast.LENGTH_SHORT).show()
                } else {
                    // Сообщение об ошибке
                    Toast.makeText(context, "❌ Ошибка при обновлении", Toast.LENGTH_SHORT).show()
                }
            }
        }.show()
    }

    // ============ ОБНОВЛЕНИЕ UI ============

    /**
     * updateTaskDisplay - обновление всех UI элементов данными задачи
     *
     * @param task задача для отображения
     *
     * Обновляет:
     * - Заголовок
     * - Описание
     * - Категорию (сложность)
     * - Статус выполнения
     * - Цвет карточки категории
     */
    private fun updateTaskDisplay(task: Task) {
        // Основная информация
        binding.tvDetailTitle.text = task.title
        binding.tvDetailDescription.text = task.description
        binding.tvDetailCategory.text = task.levels.name

        // Статус выполнения
        binding.tvDetailStatus.text = if (task.isComplete) "✓ Выполнено" else "○ В процессе"
        binding.tvDetailStatus.setTextColor(
            if (task.isComplete)
                requireContext().getColor(R.color.green)
            else
                requireContext().getColor(R.color.orange)
        )

        // Цвет карточки в зависимости от сложности
        when (task.levels) {
            LEVEL.EASY -> {
                binding.cardDetailCategory.setCardBackgroundColor(requireContext().getColor(R.color.green))
                binding.tvDetailCategory.setTextColor(requireContext().getColor(R.color.white))
            }
            LEVEL.MEDIUM -> {
                binding.cardDetailCategory.setCardBackgroundColor(requireContext().getColor(R.color.orange))
                binding.tvDetailCategory.setTextColor(requireContext().getColor(R.color.white))
            }
            LEVEL.HARD -> {
                binding.cardDetailCategory.setCardBackgroundColor(requireContext().getColor(R.color.red))
                binding.tvDetailCategory.setTextColor(requireContext().getColor(R.color.white))
            }
        }
    }
}