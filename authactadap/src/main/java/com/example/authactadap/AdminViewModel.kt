package com.example.authactadap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdminViewModel: ViewModel() {
    // Список всех пользователей
    private val _users = MutableLiveData<List<Users>>().apply { value = InMemoryUserRepository.getAllUsers() }
    val users: LiveData<List<Users>> = _users
}