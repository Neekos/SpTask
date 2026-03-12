package com.example.zachet2

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private var nextId = 1  // Простой счетчик ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Создание адаптера
        adapter = TaskAdapter(
            tasks = taskList,
            onItemClick = { task -> editTask(task) },      // Для Update
            onDeleteClick = { task -> deleteTask(task) }   // Для Delete
        )
        recyclerView.adapter = adapter

        // Кнопка добавления новой задачи (Create)
        val fab: Button = findViewById(R.id.fab)
        fab.setOnClickListener {
            addNewTask()
        }

        // Добавим несколько тестовых задач
        addTestTasks()
    }

    // CREATE: Добавить новую задачу
    private fun addNewTask() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTaskTitle)

        AlertDialog.Builder(this)
            .setTitle("Новая задача")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val title = editTextTitle.text.toString()
                if (title.isNotBlank()) {
                    val newTask = Task(id = nextId++, title = title)
                    adapter.addTask(newTask)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // UPDATE: Редактировать существующую задачу
    private fun editTask(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTaskTitle)
        editTextTitle.setText(task.title)

        AlertDialog.Builder(this)
            .setTitle("Редактировать задачу")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newTitle = editTextTitle.text.toString()
                if (newTitle.isNotBlank()) {
                    val updatedTask = task.copy(title = newTitle)
                    adapter.updateTask(updatedTask)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // DELETE: Удалить задачу
    private fun deleteTask(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Удалить задачу")
            .setMessage("Вы уверены, что хотите удалить задачу \"${task.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                adapter.deleteTask(task)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Тестовые данные
    private fun addTestTasks() {
        val testTasks = listOf(
            Task(id = nextId++, title = "Купить молоко"),
            Task(id = nextId++, title = "Сделать домашнее задание", isCompleted = true),
            Task(id = nextId++, title = "Позвонить маме")
        )
        taskList.addAll(testTasks)
        adapter.updateTasks(taskList)
    }
}