package com.example.zachet2

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private var tasks: MutableList<Task> = mutableListOf(),
    private val onItemClick: (Task) -> Unit,          // Для обновления
    private val onDeleteClick: (Task) -> Unit         // Для удаления
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // 1. ViewHolder
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val checkBoxCompleted: CheckBox = itemView.findViewById(R.id.checkBoxCompleted)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(task: Task) {
            textViewTitle.text = task.title
            checkBoxCompleted.isChecked = task.isCompleted

            // Чекбокс: обновление статуса (Update)
            checkBoxCompleted.setOnCheckedChangeListener { _, isChecked ->
                task.isCompleted = isChecked
                if (isChecked) {
                    // ДОБАВИТЬ зачеркивание
                    textViewTitle.paintFlags = textViewTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    // УБРАТЬ зачеркивание
                    textViewTitle.paintFlags = textViewTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Можно добавить здесь вызов для сохранения в БД
            }

            // Клик по элементу: редактирование (Update)
            itemView.setOnClickListener {
                onItemClick(task)
            }

            // Кнопка удаления (Delete)
            buttonDelete.setOnClickListener {
                onDeleteClick(task)
            }
        }
    }

    // 2. Методы адаптера
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    // 3. CRUD-методы
    // CREATE: Добавление новой задачи
    fun addTask(task: Task) {
        tasks.add(0, task)  // Добавляем в начало списка
        notifyItemInserted(0)
    }

    // READ: Обновление всего списка
    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    // UPDATE: Обновление конкретной задачи
    fun updateTask(updatedTask: Task) {
        val position = tasks.indexOfFirst { it.id == updatedTask.id }
        if (position != -1) {
            tasks[position] = updatedTask
            notifyItemChanged(position)
        }
    }

    // DELETE: Удаление задачи
    fun deleteTask(task: Task) {
        val position = tasks.indexOfFirst { it.id == task.id }
        if (position != -1) {
            tasks.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}