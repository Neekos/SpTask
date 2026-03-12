package com.example.fragmentlessons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataModel: ViewModel() {
    private val _messageToHome = MutableLiveData<String>()
    private val _messageToVideo = MutableLiveData<String>()

    val messageToHome: LiveData<String> = _messageToHome
    val messageToVideo: LiveData<String> = _messageToVideo

    fun sendToHome(message: String) {
        _messageToHome.value = message
    }
    fun sendToVideo(message: String) {
        _messageToVideo.value = message
    }
}