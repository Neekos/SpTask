package com.example.mybdsqlite

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private lateinit var categoryAdapter: CategoryAdapter
    private var categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        db = DatabaseHelper(requireContext())

        setupViews(view)
        loadCategories()

        return view
    }

    private fun setupViews(view: View) {
        // Кнопка добавления
        val btnAdd = view.findViewById<Button>(R.id.btnAddCategory)
        btnAdd.setOnClickListener { showCategoryDialog(null) }

        // Список категорий
        val rvCategories = view.findViewById<RecyclerView>(R.id.rvCategories)
        rvCategories.layoutManager = LinearLayoutManager(requireContext())

        categoryAdapter = CategoryAdapter(
            categories = categories,
            onItemClick = { category -> showCategoryDialog(category) },
            onItemLongClick = { category ->
                showDeleteDialog("категорию", category.name) {
                    deleteCategory(category)
                }
                true
            }
        )
        rvCategories.adapter = categoryAdapter
    }

    private fun loadCategories() {
        categories.clear()
        categories.addAll(db.getAllCategories())
        categoryAdapter.notifyDataSetChanged()
    }

    private fun showCategoryDialog(category: Category?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val etName = dialogView.findViewById<EditText>(R.id.etCategoryName)

        if (category != null) {
            etName.setText(category.name)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (category == null) "Добавить категорию" else "Редактировать категорию")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = etName.text.toString()

                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (category == null) {
                    // Добавление
                    val newCategory = Category(name = name)
                    db.addCategory(newCategory)
                    Toast.makeText(requireContext(), "Категория добавлена", Toast.LENGTH_SHORT).show()
                } else {
                    // Обновление
                    category.name = name
                    db.updateCategory(category)
                    Toast.makeText(requireContext(), "Категория обновлена", Toast.LENGTH_SHORT).show()
                }

                loadCategories()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteDialog(type: String, name: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Удалить $type '$name'?")
            .setPositiveButton("Удалить") { _, _ -> onConfirm() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        db.deleteCategory(category.id)
        Toast.makeText(requireContext(), "Категория удалена", Toast.LENGTH_SHORT).show()
        loadCategories()
    }
}