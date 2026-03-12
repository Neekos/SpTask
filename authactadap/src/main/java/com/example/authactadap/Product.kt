package com.example.authactadap

import android.os.Parcelable
import androidx.versionedparcelable.VersionedParcelize
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val title:String,
    val desc:String,
    val price:Double
): Parcelable
