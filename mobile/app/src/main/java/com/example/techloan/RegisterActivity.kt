package com.example.techloan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.databinding.ActivityRegisterBinding
import com.example.techloan.viewmodel.AuthState
import com.example.techloan.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val name = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()
                viewModel.register(name, email, password)
            }
        }

        binding.btnGoToLogin.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Clear previous errors
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tvError.visibility = View.GONE
        binding.tvSuccess.visibility = View.GONE

        if (name.isEmpty()) {
            binding.tilName.error = "Full name is required"
            isValid = false
        } else if (name.length < 2) {
            binding.tilName.error = "Name must be at least 2 characters"
            isValid = false
        }

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
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                    binding.tvError.visibility = View.GONE
                    binding.tvSuccess.visibility = View.GONE
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true

                    // Show success message
                    binding.tvSuccess.text = "Account created successfully! Redirecting to login..."
                    binding.tvSuccess.visibility = View.VISIBLE

                    // Navigate to Login after a short delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }, 1500)
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }
                is AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }
}