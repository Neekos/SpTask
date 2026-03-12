package com.example.pz4

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductRecyclerAdapter
    private lateinit var fabAdd: FloatingActionButton

    companion object {
        val products = mutableListOf<Product>()
        var nextId = 1

        init {
            // Тестовые данные
            addTestProducts()
        }

        private fun addTestProducts() {
            products.addAll(listOf(
                Product(nextId++, "Ноутбук", "Мощный игровой ноутбук", 1500.0, "https://via.placeholder.com/300"),
                Product(nextId++, "Смартфон", "Флагманский смартфон", 1000.0, "https://via.placeholder.com/300"),
                Product(nextId++, "Наушники", "Беспроводные наушники", 200.0, "https://via.placeholder.com/300"),
                Product(nextId++, "Монитор", "4K монитор 27 дюймов", 500.0, "https://picsum.photos/300/200?random=2"),
                Product(nextId++, "Клавиатура", "Механическая клавиатура", 150.0, "https://picsum.photos/300/200?random=1"),
                Product(nextId++, "Мышь", "Игровая мышь", 80.0, "https://i07.fotocdn.net/s213/0f71041411cd5d8b/public_pin_l/2821550865.jpg")
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
        // Установка GridLayoutManager для 2 колонок
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        // Создание адаптера
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