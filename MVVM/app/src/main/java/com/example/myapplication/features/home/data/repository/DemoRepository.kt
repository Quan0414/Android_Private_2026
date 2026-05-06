package com.example.myapplication.features.home.data.repository

import kotlinx.coroutines.delay

class DemoRepository {
    suspend fun login(username: String, password: String): Boolean {
        delay(2000)
        return username == "user" && password == "password"
    }
}