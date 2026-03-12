package com.example.arrayadapterproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val dataSet: Array<String>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    // 4. ViewHolder класс
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {

            textView = view.findViewById(R.id.textView)
        }
    }

    // 1. Создание ViewHolder
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view)
    }
    // 2. Привязка данных к ViewHolder
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = dataSet[position]
        // Обработка клика на элементе
        viewHolder.textView.setOnClickListener {
            // position может быть неактуальной из-за переиспользования ViewHolder
            val currentPosition = viewHolder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val item = dataSet[currentPosition]
                // Действие при клике
                Toast.makeText(viewHolder.itemView.context, "Clicked: $item", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 3. Количество элементов
    override fun getItemCount() = dataSet.size
}