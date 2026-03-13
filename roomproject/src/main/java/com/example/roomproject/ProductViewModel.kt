package com.example.roomproject

// ProductViewModel.kt
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData  // Добавьте этот импорт
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    // Преобразуем Flow в LiveData
    val allProducts: LiveData<List<Product>> = repository.allProducts.asLiveData()

    fun insertProduct(product: Product) {
        viewModelScope.launch {
            repository.insert(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.update(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    suspend fun getProductById(id: Int): Product? {
        return repository.getProductById(id)
    }
}