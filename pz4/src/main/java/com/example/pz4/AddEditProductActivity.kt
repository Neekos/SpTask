package com.example.pz4

import android.app.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class AddEditProductActivity : AppCompatActivity() {
    lateinit var etTitle: TextView
    lateinit var etDescription: TextView
    lateinit var etPrice: TextView
    lateinit var etImageUrl: EditText
    lateinit var btnSave: Button
    lateinit var btnCancel: Button

    private var isEditMode = false
    private var currentProductId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)
        initViews()
        checkEditMode()
        setupListeners()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        etImageUrl = findViewById(R.id.etImageUrl)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun checkEditMode() {
        currentProductId = intent.getIntExtra("PRODUCT_ID", -1)

        if (currentProductId != -1) {
            isEditMode = true
            val product = MainActivity.products.find { it.id == currentProductId }
            product?.let {
                fillFormWithProductData(it)
                supportActionBar?.title = "Редактирование товара"
            }
        } else {
            supportActionBar?.title = "Добавление товара"
        }
    }

    private fun fillFormWithProductData(product: Product) {
        etTitle.setText(product.title)
        etDescription.setText(product.description)
        etPrice.setText(product.price.toString())
        etImageUrl.setText(product.imageUrl)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            if (validateForm()) {
                saveProduct()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (etTitle.text.toString().trim().isEmpty()) {
            etTitle.error = "Введите название товара"
            isValid = false
        }

        if (etPrice.text.toString().trim().isEmpty()) {
            etPrice.error = "Введите цену"
            isValid = false
        } else if (etPrice.text.toString().toDoubleOrNull() == null) {
            etPrice.error = "Введите корректную цену"
            isValid = false
        }

        return isValid
    }

    private fun saveProduct() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val price = etPrice.text.toString().toDouble()
        val imageUrl = etImageUrl.text.toString().trim()

        if (isEditMode) {
            // Редактирование существующего товара
            val updatedProduct = Product(currentProductId, title, description, price, imageUrl)
            MainActivity.updateProduct(currentProductId, updatedProduct)
            Toast.makeText(this, "Товар обновлен", Toast.LENGTH_SHORT).show()
        } else {
            // Добавление нового товара
            val newProduct = Product(MainActivity.nextId, title, description, price, imageUrl)
            MainActivity.addNewProduct(newProduct)
            Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}