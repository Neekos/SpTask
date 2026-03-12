package com.example.pz4nointernet

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ProductDetailActivity : AppCompatActivity() {

    private lateinit var productDetailImage: ImageView
    private lateinit var productDetailTitle: TextView
    private lateinit var productDetailDescription: TextView
    private lateinit var productDetailPrice: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        setupProductDetails()
        setupListeners()
    }

    private fun initViews() {
        productDetailImage = findViewById(R.id.productDetailImage)
        productDetailTitle = findViewById(R.id.productDetailTitle)
        productDetailDescription = findViewById(R.id.productDetailDescription)
        productDetailPrice = findViewById(R.id.productDetailPrice)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupProductDetails() {
        val productId = intent.getIntExtra("PRODUCT_ID", -1)
        val product = MainActivity.products.find { it.id == productId }

        product?.let {
            supportActionBar?.title = it.title
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            // Загрузка изображения из ресурсов
            productDetailImage.setImageResource(it.imageResId)

            productDetailTitle.text = it.title
            productDetailDescription.text = it.description
            productDetailPrice.text = "${it.price}, Руб"
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}