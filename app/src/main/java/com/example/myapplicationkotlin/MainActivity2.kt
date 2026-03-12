package com.example.myapplicationkotlin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        val btnBack = findViewById<Button>(R.id.button)

        val listView: ListView = findViewById(R.id.listView)

        val temp = intent.getStringArrayListExtra("fruitsName")

        val myList = mutableListOf<String>()

        temp?.forEach { it->
            myList.add(it)
        }

        var adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, myList)

        listView.adapter = adapter

        btnBack.setOnClickListener {
            finish()
        }
    }
}