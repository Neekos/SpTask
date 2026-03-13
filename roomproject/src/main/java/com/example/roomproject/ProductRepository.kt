package com.example.roomproject

// ProductRepository.kt
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }
}