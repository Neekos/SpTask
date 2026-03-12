package com.example.arrayadapterproject

import android.R.attr.resource
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class MyArrayAdapter<T>(context: Context, resource: Int, objects: List<T>) :
    ArrayAdapter<T>(context, resource, objects) {

    // Главный метод - создает/переиспользует View и заполняет данными
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val item = getItem(position)
        // Заполнение View данными...
        return view
    }

    // Получить объект по позиции
    override fun getItem(position: Int): T? {
        return super.getItem(position)
    }

    // Количество элементов
    override fun getCount(): Int {
        return super.getCount()
    }

    // ID элемента (по умолчанию = position)
    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
}