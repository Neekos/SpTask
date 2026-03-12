package com.example.mybdsqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "shop.db", null, 4) {

    companion object {
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY_ID = "category_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_CATEGORIES (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT)")

        db.execSQL("CREATE TABLE $TABLE_PRODUCTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_IMAGE TEXT, " +
                "$COLUMN_PRICE REAL, " +
                "$COLUMN_CATEGORY_ID INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    // Категории
    fun addCategory(category: Category): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, category.name)
        val id = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return id
    }

    fun getAllCategories(): MutableList<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, "$COLUMN_NAME ASC")

        while (cursor.moveToNext()) {
            list.add(Category(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            ))
        }
        cursor.close()
        db.close()
        return list
    }

    fun updateCategory(category: Category): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, category.name)
        val result = db.update(TABLE_CATEGORIES, values, "$COLUMN_ID = ?", arrayOf(category.id.toString()))
        db.close()
        return result
    }

    fun deleteCategory(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_CATEGORIES, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Продукты
    fun addProduct(product: Product): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, product.name)
        values.put(COLUMN_IMAGE, product.image)
        values.put(COLUMN_PRICE, product.price)
        values.put(COLUMN_CATEGORY_ID, product.categoryId)
        val id = db.insert(TABLE_PRODUCTS, null, values)
        db.close()
        return id
    }

    fun getAllProductsWithCategories(): MutableList<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, "$COLUMN_NAME ASC")

        while (cursor.moveToNext()) {
            val product = Product(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            )
            val category = getCategoryById(product.categoryId)
            product.categoryName = category?.name ?: "Без категории"
            list.add(product)
        }
        cursor.close()
        db.close()
        return list
    }

    fun getProductsByCategory(categoryId: Int): MutableList<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString()),
            null, null, "$COLUMN_NAME ASC"
        )

        while (cursor.moveToNext()) {
            val product = Product(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            )
            val category = getCategoryById(product.categoryId)
            product.categoryName = category?.name ?: "Без категории"
            list.add(product)
        }
        cursor.close()
        db.close()
        return list
    }

    fun getCategoryById(id: Int): Category? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var category: Category? = null
        if (cursor.moveToFirst()) {
            category = Category(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            )
        }
        cursor.close()
        db.close()
        return category
    }

    fun updateProduct(product: Product): Int {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, product.name)
        values.put(COLUMN_IMAGE, product.image)
        values.put(COLUMN_PRICE, product.price)
        values.put(COLUMN_CATEGORY_ID, product.categoryId)
        val result = db.update(TABLE_PRODUCTS, values, "$COLUMN_ID = ?", arrayOf(product.id.toString()))
        db.close()
        return result
    }

    fun deleteProduct(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_PRODUCTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
}