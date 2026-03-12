package com.example.pz4nointernet

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var adapter: ProductRecyclerAdapter

    companion object {
        val products = mutableListOf<Product>()
        var nextId = 1

        init {
            // Тестовые данные с изображениями из ресурсов
            addTestProducts()
        }

        private fun addTestProducts() {
            products.addAll(listOf(
                Product(nextId++, "Ноутбук", "Мощный игровой ноутбук", 1500.0,
                    R.drawable.product_laptop),
                Product(nextId++, "Смартфон", "Флагманский смартфон", 1000.0,
                    R.drawable.product_phone),
                Product(nextId++, "Наушники", "Беспроводные наушники", 200.0,
                    R.drawable.product_headphones),
                Product(nextId++, "Монитор", "4K монитор 27 дюймов", 500.0,
                    R.drawable.product_monitor),
                Product(nextId++, "Клавиатура", "Механическая клавиатура", 150.0,
                    R.drawable.product_keyboard),
                Product(nextId++, "Мышь", "Игровая мышь", 80.0,
                    R.drawable.product_mouse)
            ))
        }

        fun addNewProduct(product: Product) {
            products.add(product)
            nextId++
        }

        fun updateProduct(productId: Int, updatedProduct: Product) {
            val index = products.indexOfFirst { it.id == productId }
            if (index != -1) {
                products[index] = updatedProduct
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        adapter = ProductRecyclerAdapter(products) { product ->
            openProductDetail(product)
        }

        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        fabAdd.setOnClickListener {
            openAddProductScreen()
        }
    }

    private fun openProductDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("PRODUCT_ID", product.id)
        startActivity(intent)
    }

    private fun openAddProductScreen() {
        val intent = Intent(this, AddEditProductActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}