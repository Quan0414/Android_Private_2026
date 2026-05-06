package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.myapplication.core.database.db.AppDatabase
import com.example.myapplication.core.database.entity.User
import com.example.myapplication.core.ui.UiState
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.features.home.data.repository.DemoRepository
import com.example.myapplication.features.home.ui.viewmodel.MainViewModel
import com.example.myapplication.features.home.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(DemoRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            viewModel.login(username, password)
        }

        viewModel.loginResult.observe(this) { state ->
            when (state) {
                is UiState.Idle -> binding.tvResult.text = "Idle"
                is UiState.Loading -> binding.tvResult.text = "Loading..."
                is UiState.Success -> binding.tvResult.text = "Login successful!"
                is UiState.Error -> binding.tvResult.text = "Login failed: ${state.message}"
            }
        }
//
//        val db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java, "database-name"
//        ).build()
//
//        val userDao = db.userDao()
//
//        lifecycleScope.launch {
//            val user = withContext(Dispatchers.IO) {
//                val newUser = User(uid = 1, firstName = "John", lastName = "Doe")
//                userDao.insertAll(newUser)   // insert trước
//                userDao.findByName("John", "Doe")
//            }
//
//            binding.tvResult.append("\nRetrieved User: ${user.firstName} ${user.lastName}")
//        }
    }
}