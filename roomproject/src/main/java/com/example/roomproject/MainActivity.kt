package com.example.roomproject
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomproject.databinding.ActivityMainBinding
import com.example.roomproject.databinding.DialogProductBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ViewModel
        setupViewModel()

        // Настройка RecyclerView
        setupRecyclerView()

        // Наблюдение за изменениями в списке продуктов
        viewModel.allProducts.observe(this, Observer { products ->
            adapter.updateList(products)
        })

        // Обработка нажатия на FAB для добавления нового продукта
        binding.fabAdd.setOnClickListener {
            showProductDialog(null)
        }
    }

    private fun setupViewModel() {
        val database = ProductDatabase.getDatabase(this)
        val repository = ProductRepository(database.productDao())

        // Создание фабрики для ViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(repository) as T
            }
        }

        viewModel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            emptyList(),
            { product -> showProductDialog(product) },  // Клик для редактирования
            { product -> showDeleteDialog(product) } // Долгий клик для удаления
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    // Диалог для создания/редактирования продукта
    private fun showProductDialog(product: Product?) {
        val dialogBinding = DialogProductBinding.inflate(layoutInflater)
        val isEditMode = product != null

        // Устанавливаем заголовок и заполняем поля, если это редактирование
        dialogBinding.apply {
            tvDialogTitle.text = if (isEditMode) "Редактировать продукт" else "Добавить продукт"

            if (isEditMode) {
                etTitle.setText(product.title)
                etDescription.setText(product.description)
                etPrice.setText(product.price.toString())
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            // Получаем данные из полей
            val title = dialogBinding.etTitle.text.toString()
            val description = dialogBinding.etDescription.text.toString()
            val priceText = dialogBinding.etPrice.text.toString()

            // Валидация
            if (title.isEmpty() || description.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDoubleOrNull()
            if (price == null) {
                Toast.makeText(this, "Введите корректную цену", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                // Обновляем существующий продукт
                val updatedProduct = product.copy(
                    title = title,
                    description = description,
                    price = price
                )
                viewModel.updateProduct(updatedProduct)
                Toast.makeText(this, "Продукт обновлен", Toast.LENGTH_SHORT).show()
            } else {
                // Создаем новый продукт
                val newProduct = Product(
                    title = title,
                    description = description,
                    price = price
                )
                viewModel.insertProduct(newProduct)
                Toast.makeText(this, "Продукт добавлен", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // Диалог подтверждения удаления
    private fun showDeleteDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Удаление продукта")
            .setMessage("Вы уверены, что хотите удалить '${product.title}'?")
            .setPositiveButton("Да") { _, _ ->
                viewModel.deleteProduct(product)
                Toast.makeText(this, "Продукт удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Нет", null)
            .show()
    }
}