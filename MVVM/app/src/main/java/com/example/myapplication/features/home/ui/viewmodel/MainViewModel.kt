package com.example.myapplication.features.home.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.features.home.data.repository.DemoRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: DemoRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val result = repo.login(username, password)
            _loginResult.value = result
        }
    }

}