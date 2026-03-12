package com.example.arrayadapterproject

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView: ListView = findViewById(R.id.listview)

//        val cities = listOf("Москва", "Санкт-Петербург", "Новосибирск", "Екатеринбург")
//
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
//        listView.adapter = adapter
//
//
//        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            // position - это индекс выбранного элемента в списке
//            val selectedCity = cities[position]
//            Toast.makeText(this, "Выбран город: $selectedCity", Toast.LENGTH_SHORT).show()
//        }


//        ========================================================= кастомный



        val citiesList = listOf(
            City("Москва"),
            City("Санкт-Петербург"),
            City("Новосибирск"),
            City("Екатеринбург")
        )

        val adapterCustom = CityAdapter(this, citiesList)
        listView.adapter = adapterCustom

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedCities = citiesList[position]
            Toast.makeText(this, "Выбран: ${selectedCities.title}", Toast.LENGTH_SHORT).show()
        }






    }
}