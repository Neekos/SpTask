package com.example.spprojectsqlitetask.adapters

// ============ ИМПОРТЫ ============
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spprojectsqlitetask.R
import com.example.spprojectsqlitetask.data.LEVEL
import com.example.spprojectsqlitetask.data.Task
import com.example.spprojectsqlitetask.databinding.CardItemBinding

/**
 * TaskAdapter - адаптер для RecyclerView, отображающий список задач
 *
 * Наследуется от ListAdapter, который уже содержит DiffUtil для оптимального обновления
 *
 * @param onTaskCheckedChange - колбэк при изменении состояния чекбокса
 * @param onItemClick - колбэк при клике на карточку задачи
 * @param onItemLongClick - колбэк при долгом нажатии на карточку
 */
class TaskAdapter(
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit,
    private val onItemLongClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.ViewHolder>(TaskDiffCallback()) {

    /**
     * ViewHolder - внутренний класс, который содержит ссылки на элементы карточки задачи
     * Использует ViewBinding для доступа к View
     *
     * @param binding - сгенерированный класс CardItemBinding
     */
    class ViewHolder(private val binding: CardItemBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * bind - привязка данных задачи к элементам ViewHolder
         *
         * @param task - объект задачи для отображения
         * @param onTaskCheckedChange - колбэк для чекбокса
         * @param onItemClick - колбэк для клика
         * @param onItemLongClick - колбэк для долгого нажатия
         */
        fun bind(
            task: Task,
            onTaskCheckedChange: (Task, Boolean) -> Unit,
            onItemClick: (Task) -> Unit,
            onItemLongClick: (Task) -> Unit
        ) {
            // ============ ВАЖНО: ПРАВИЛЬНАЯ РАБОТА С ЧЕКБОКСОМ ============
            // Убираем старый listener перед изменением состояния
            // Это предотвращает множественные вызовы при переиспользовании ViewHolder
            binding.check.setOnCheckedChangeListener(null)

            // ============ УСТАНОВКА ДАННЫХ ============
            binding.check.isChecked = task.isComplete
            binding.title.text = task.title

            // Обрезаем описание до 50 символов, чтобы карточки были одинакового размера
            binding.desc.text = if (task.description.length > 50) {
                task.description.substring(0, 47) + "..."  // 47 символов + "..."
            } else {
                task.description
            }

            binding.category.text = task.levels.name

            // ============ ВИЗУАЛЬНЫЕ ЭФФЕКТЫ ============
            updateCategoryColor(task.levels)
            updateTextStrikeThrough(task.isComplete)

            // ============ НОВЫЙ LISTENER ДЛЯ ЧЕКБОКСА ============
            // Добавляем после установки всех данных
            binding.check.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChange(task, isChecked)
            }

            // ============ ОБРАБОТЧИКИ СОБЫТИЙ ============
            // Клик на всю карточку
            binding.root.setOnClickListener {
                onItemClick(task)
            }

            // Долгое нажатие на карточку
            binding.root.setOnLongClickListener {
                onItemLongClick(task)
                true // Возвращаем true, чтобы показать, что событие обработано
            }
        }

        /**
         * updateCategoryColor - обновление цвета карточки категории в зависимости от сложности
         *
         * @param level - уровень сложности (EASY, MEDIUM, HARD)
         */
        private fun updateCategoryColor(level: LEVEL) {
            val context = binding.root.context
            val bgColor = when (level) {
                LEVEL.EASY -> R.color.green      // Зеленый для легких задач
                LEVEL.MEDIUM -> R.color.orange   // Оранжевый для средних
                LEVEL.HARD -> R.color.red        // Красный для сложных
            }

            binding.categoryCard.setCardBackgroundColor(ContextCompat.getColor(context, bgColor))
            binding.category.setTextColor(Color.WHITE) // Белый текст на цветном фоне
        }

        /**
         * updateTextStrikeThrough - обновление зачеркивания текста в зависимости от статуса
         *
         * @param isComplete - true если задача выполнена, false если нет
         *
         * Для выполненных задач:
         * - Текст зачеркивается (STRIKE_THRU_TEXT_FLAG)
         * - Становится полупрозрачным (alpha = 0.6)
         *
         * Для невыполненных:
         * - Зачеркивание убирается
         * - Полная непрозрачность (alpha = 1.0)
         */
        private fun updateTextStrikeThrough(isComplete: Boolean) {
            if (isComplete) {
                // Добавляем флаг зачеркивания (побитовая операция OR)
                binding.title.paintFlags = binding.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.desc.paintFlags = binding.desc.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.category.paintFlags = binding.category.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                // Делаем текст полупрозрачным
                binding.title.alpha = 0.6f
                binding.desc.alpha = 0.6f
                binding.category.alpha = 0.6f
            } else {
                // Убираем флаг зачеркивания (побитовая операция AND с инверсией)
                binding.title.paintFlags = binding.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.desc.paintFlags = binding.desc.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.category.paintFlags = binding.category.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                // Возвращаем полную непрозрачность
                binding.title.alpha = 1.0f
                binding.desc.alpha = 1.0f
                binding.category.alpha = 1.0f
            }
        }
    }

    /**
     * onCreateViewHolder - создание нового ViewHolder
     *
     * @param parent - родительский ViewGroup (RecyclerView)
     * @param viewType - тип view (не используется, т.к. все элементы одинаковые)
     * @return новый ViewHolder с раздутым layout
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * onBindViewHolder - привязка данных к ViewHolder на определенной позиции
     *
     * @param holder - ViewHolder для заполнения
     * @param position - позиция в списке
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            getItem(position),  // getItem - метод ListAdapter для получения элемента по позиции
            onTaskCheckedChange,
            onItemClick,
            onItemLongClick
        )
    }
}

/**
 * TaskDiffCallback - класс для сравнения элементов списка при обновлении
 *
 * DiffUtil - утилита от Google, которая вычисляет разницу между старым и новым списком
 * и обновляет только изменившиеся элементы, что повышает производительность
 */
class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {

    /**
     * areItemsTheSame - проверяет, является ли элемент тем же самым объектом
     *
     * @param oldItem - старый элемент
     * @param newItem - новый элемент
     * @return true если это та же задача (по id)
     */
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        // Сравниваем по ID, так как это уникальный идентификатор
        return oldItem.id == newItem.id
    }

    /**
     * areContentsTheSame - проверяет, изменилось ли содержимое элемента
     *
     * @param oldItem - старый элемент
     * @param newItem - новый элемент
     * @return true если содержимое не изменилось
     */
    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        // Сравниваем полностью содержимое
        // Data class автоматически генерирует equals()
        return oldItem == newItem
    }

    /**
     * getChangePayload - возвращает информацию о конкретных изменениях
     *
     * @param oldItem - старый элемент
     * @param newItem - новый элемент
     * @return объект с изменениями для анимированного обновления
     */
    override fun getChangePayload(oldItem: Task, newItem: Task): Any? {
        // Можно вернуть объект с изменениями для частичного обновления
        // Например, если изменился только статус, можно обновить только чекбокс
        return null // Пока не используем
    }
}