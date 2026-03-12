package com.example.authactadap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val ptitle = findViewById<EditText>(R.id.pTitle)
        val pdesc = findViewById<EditText>(R.id.pDesc)
        val pprice = findViewById<EditText>(R.id.pPrice)
        val pbtn = findViewById<Button>(R.id.btnProduct)


        pbtn.setOnClickListener {
            val tit = ptitle.text.toString()
            val desc = pdesc.text.toString()
            val price = pprice.text.toString().toDoubleOrNull()?:0.0

            val product = Product(tit, desc, price)
            val intent = Intent(this, ProductsListActivity::class.java).apply {
                putExtra("PRODUCT", product)
            }
            startActivity(intent)
        }



    }
}