package com.example.techloan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.databinding.ActivityLoginBinding
import com.example.techloan.viewmodel.AuthState
import com.example.techloan.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()
                viewModel.login(email, password)
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Clear previous errors
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tvError.visibility = View.GONE

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.tvError.visibility = View.GONE
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true

                    // Save token to SharedPreferences
                    val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
                    prefs.edit().putString("jwt_token", state.token).apply()

                    // Navigate to Dashboard
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
                is AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }
}