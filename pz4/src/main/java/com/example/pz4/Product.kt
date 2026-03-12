package com.example.pz4

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val id: Int,
    var title: String,
    var description: String,
    var price: Double,
    var imageUrl: String = ""
) : Parcelable