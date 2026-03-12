package com.example.pz4nointernet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



class AddEditProductActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var productImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var isEditMode = false
    private var currentProductId = -1
    private var selectedImageResId: Int = R.drawable.placeholder_image

    companion object {
        const val REQUEST_CODE_IMAGE_PICKER = 100
    }

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
        productImage = findViewById(R.id.productImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
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
            // Устанавливаем случайное изображение для нового товара
            selectedImageResId = ImageManager.getRandomImageResId()
            productImage.setImageResource(selectedImageResId)
        }
    }

    private fun fillFormWithProductData(product: Product) {
        etTitle.setText(product.title)
        etDescription.setText(product.description)
        etPrice.setText(product.price.toString())
        selectedImageResId = product.imageResId
        productImage.setImageResource(selectedImageResId)
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            if (validateForm()) {
                saveProduct()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val imageResId = data?.getIntExtra("SELECTED_IMAGE_RES_ID", R.drawable.placeholder_image)
            imageResId?.let {
                selectedImageResId = it
                productImage.setImageResource(selectedImageResId)
            }
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

        if (isEditMode) {
            // Редактирование существующего товара
            val updatedProduct = Product(currentProductId, title, description, price, selectedImageResId)
            MainActivity.updateProduct(currentProductId, updatedProduct)
            Toast.makeText(this, "Товар обновлен", Toast.LENGTH_SHORT).show()
        } else {
            // Добавление нового товара
            val newProduct = Product(MainActivity.nextId, title, description, price, selectedImageResId)
            MainActivity.addNewProduct(newProduct)
            Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }
}