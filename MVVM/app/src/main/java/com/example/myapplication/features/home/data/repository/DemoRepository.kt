package com.example.myapplication.features.home.data.repository

class DemoRepository {
    fun login(username: String, password: String): Boolean {
        return username == "user" && password == "password"
    }
}