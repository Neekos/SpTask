package com.example.authactadap

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProductsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_products_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pView = findViewById<TextView>(R.id.textView)

        // API 33+
        //intent.getParcelableExtra("USER_KEY", User::class.java)
        // API < 33 (старый метод)
        @Suppress("DEPRECATION")
        val product: Product? = intent.getParcelableExtra("PRODUCT")

        if (product!=null){
            pView.text = "Добавлен товар!\n" +
                    "Название: ${product.title}\n"+
                    "Описание: ${product.desc}\n"+
                    "Цена: ${product.price}"
        }
    }
}