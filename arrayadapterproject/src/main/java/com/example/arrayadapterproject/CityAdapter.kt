package com.example.arrayadapterproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.coroutines.NonCancellable.parent

class CityAdapter(context: Context, citys: List<City>) :
    ArrayAdapter<City>(context, R.layout.city_item, citys) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.city_item, parent, false)

        getItem(position)?.let { city ->
            view.findViewById<TextView>(R.id.textView).text = city.title
            //view.findViewById<TextView>(R.id.textView2).text2 = city.title2 тут поля связываем
        }
        return view
    }
}