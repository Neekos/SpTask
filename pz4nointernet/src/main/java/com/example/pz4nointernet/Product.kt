package com.example.pz4nointernet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    var title: String,
    var description: String,
    var price: Double,
    var imageResId: Int = R.drawable.placeholder_image
) : Parcelable