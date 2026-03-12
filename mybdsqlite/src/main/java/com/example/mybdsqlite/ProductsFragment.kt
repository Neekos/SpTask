package com.example.mybdsqlite

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ProductsFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private lateinit var productAdapter: ProductAdapter
    private var products = mutableListOf<Product>()
    private var categories = mutableListOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_products, container, false)

        db = DatabaseHelper(requireContext())

        setupViews(view)
        loadData()

        return view
    }

    private fun setupViews(view: View) {
        // Кнопка добавления
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddProduct)
        btnAdd.setOnClickListener { showProductDialog(null) }

        // Список продуктов
        val rvProducts = view.findViewById<RecyclerView>(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(requireContext())

        productAdapter = ProductAdapter(
            products = products,
            onItemClick = { product -> showProductDialog(product) },
            onItemLongClick = { product ->
                showDeleteDialog("продукт", product.name) {
                    deleteProduct(product)
                }
                true
            }
        )
        rvProducts.adapter = productAdapter

        // Спиннер для фильтрации
        setupCategorySpinner(view)
    }

    private fun setupCategorySpinner(view: View) {
        val spinner = view.findViewById<Spinner>(R.id.spinnerCategory)

        // Обновляем список категорий
        updateSpinnerData(spinner)

        // Обработка выбора
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    loadProducts()
                } else {
                    val catId = categories[position - 1].id
                    loadProductsByCategory(catId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSpinnerData(spinner: Spinner) {
        categories = db.getAllCategories()
        val categoryList = mutableListOf("Все категории")
        categoryList.addAll(categories.map { it.name })

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun loadData() {
        loadProducts()
        categories = db.getAllCategories()
    }

    private fun loadProducts() {
        products.clear()
        products.addAll(db.getAllProductsWithCategories())
        productAdapter.notifyDataSetChanged()
    }

    private fun loadProductsByCategory(categoryId: Int) {
        products.clear()
        products.addAll(db.getProductsByCategory(categoryId))
        productAdapter.notifyDataSetChanged()
    }

    private fun showProductDialog(product: Product?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)

        val etName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etPrice = dialogView.findViewById<EditText>(R.id.etProductPrice)
        val actvCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerDialogCategory)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        // Загружаем категории в AutoCompleteTextView
        val cats = db.getAllCategories()
        val catNames = cats.map { it.name }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, catNames)
        actvCategory.setAdapter(adapter)

        if (product != null) {
            etName.setText(product.name)
            etPrice.setText(product.price.toString())

            val index = cats.indexOfFirst { it.id == product.categoryId }
            if (index >= 0) {
                actvCategory.setText(catNames[index], false)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (product == null) "Добавить продукт" else "Редактировать продукт")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)
            .create()

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val priceText = etPrice.text.toString()
            val selectedCat = actvCategory.text.toString()

            if (name.isEmpty() || priceText.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val price = priceText.toDouble()
                val catId = cats.find { it.name == selectedCat }?.id ?: 0

                if (product == null) {
                    val newProduct = Product(
                        name = name,
                        image = "",
                        price = price,
                        categoryId = catId
                    )
                    db.addProduct(newProduct)
                    Toast.makeText(requireContext(), "Продукт добавлен", Toast.LENGTH_SHORT).show()
                } else {
                    product.name = name
                    product.price = price
                    product.categoryId = catId
                    db.updateProduct(product)
                    Toast.makeText(requireContext(), "Продукт обновлен", Toast.LENGTH_SHORT).show()
                }

                loadProducts()

                val spinnerView = view?.findViewById<Spinner>(R.id.spinnerCategory)
                spinnerView?.let { updateSpinnerData(it) }

                dialog.dismiss()

            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Некорректная цена", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteDialog(type: String, name: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Удалить $type '$name'?")
            .setPositiveButton("Удалить") { _, _ -> onConfirm() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        db.deleteProduct(product.id)
        Toast.makeText(requireContext(), "Продукт удален", Toast.LENGTH_SHORT).show()
        loadProducts()
    }
}