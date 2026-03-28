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
                val fullName = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim().lowercase()
                val studentId = binding.etStudentId.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val confirmPassword = binding.etConfirmPassword.text.toString().trim()
                val role = binding.spinnerRole.selectedItem.toString()
                viewModel.register(fullName, email, studentId, password, confirmPassword, role)
            }
        }

        binding.btnGoToLogin.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val fullName = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Clear previous errors
        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilStudentId.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tvError.visibility = View.GONE
        binding.tvSuccess.visibility = View.GONE

        if (fullName.isEmpty()) {
            binding.tilName.error = "Full name is required"
            isValid = false
        } else if (fullName.length < 2) {
            binding.tilName.error = "Name must be at least 2 characters"
            isValid = false
        } else if (fullName.length > 100) {
            binding.tilName.error = "Name must not exceed 100 characters"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        } else if (!email.toLowerCase().endsWith("@cit.edu")) {
            binding.tilEmail.error = "Email must be a CIT-U institutional email (@cit.edu)"
            isValid = false
        }

        if (studentId.isEmpty()) {
            binding.tilStudentId.error = "Student/Faculty ID is required"
            isValid = false
        } else if (studentId.length > 20) {
            binding.tilStudentId.error = "ID must not exceed 20 characters"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        } else if (!password.matches(Regex("^[A-Za-z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*$"))) {
            binding.tilPassword.error = "Password contains invalid characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirm password is required"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        val selectedRole = binding.spinnerRole.selectedItem.toString()
        if (selectedRole == "Select Account Type") {
            // Show error - role not selected
            isValid = false
            // Find the spinner and show a toast or message
            android.widget.Toast.makeText(this, "Please select an account type (Student or Faculty)", android.widget.Toast.LENGTH_SHORT).show()
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