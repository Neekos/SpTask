package com.example.myapplicationkotlin

import android.content.Context
import android.widget.Toast

class Fruits (name:String) {
    private  val _name = name
    override fun toString(): String {
        return _name
    }
    fun ShowName():String{
        return  _name.toString()
    }
}