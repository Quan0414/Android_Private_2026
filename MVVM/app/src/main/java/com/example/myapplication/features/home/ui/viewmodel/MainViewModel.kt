package com.example.myapplication.features.home.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.core.ui.UiState
import com.example.myapplication.features.home.data.repository.DemoRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: DemoRepository
) : ViewModel() {

    private val _loginResult = MutableLiveData<UiState<Boolean>>()
    val loginResult: LiveData<UiState<Boolean>> get() = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = UiState.Loading
            when (repo.login(username, password)) {
                true -> _loginResult.value = UiState.Success(true)
                false -> _loginResult.value = UiState.Error("Invalid credentials")
            }
        }
    }

}