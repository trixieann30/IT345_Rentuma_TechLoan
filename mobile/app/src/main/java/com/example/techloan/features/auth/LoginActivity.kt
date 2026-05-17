package com.example.techloan.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.databinding.ActivityLoginBinding
import com.example.techloan.features.custodian.CustodianDashboardActivity
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.shared.model.UserDto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            } else {
                showError("Google sign-in failed: no ID token received")
            }
        } catch (e: ApiException) {
            showError("Google sign-in failed (code ${e.statusCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("428918354170-05e2q5d7l3uvbqeqngtje2fibcnr5kap.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                viewModel.login(
                    binding.etEmail.text.toString().trim().lowercase(),
                    binding.etPassword.text.toString().trim()
                )
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            binding.tvError.visibility = View.GONE
            showLoading()
            googleSignInClient.signOut().addOnCompleteListener {
                signInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading()
                is AuthState.Success -> {
                    hideLoading()
                    val user = state.user ?: return@observe
                    saveSessionAndNavigate(state.token, user)
                }
                is AuthState.Error -> {
                    hideLoading()
                    showError(state.message)
                }
                is AuthState.Idle -> hideLoading()
            }
        }

        viewModel.forgotPasswordState.observe(this) { state ->
            when (state) {
                is ForgotPasswordState.Success ->
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                is ForgotPasswordState.Error ->
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                else -> {}
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val input = EditText(this).apply {
            hint = "Enter your CIT-U email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48, 32, 48, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your registered email and we'll send you a reset link.")
            .setView(input)
            .setPositiveButton("Send Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) viewModel.forgotPassword(email)
                else Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSessionAndNavigate(token: String, user: UserDto) {
        getSharedPreferences("techloan_prefs", MODE_PRIVATE).edit()
            .putString("jwt_token", token)
            .putLong("user_id", user.id ?: 0L)
            .putString("user_name", user.fullName ?: "")
            .putString("user_email", user.email ?: "")
            .putString("user_role", user.role ?: "")
            .apply()

        binding.tvSuccess.text = "Sign-in successful!"
        binding.tvSuccess.visibility = View.VISIBLE

        binding.root.postDelayed({
            val target = if (user.role == "CUSTODIAN") CustodianDashboardActivity::class.java
                         else DashboardActivity::class.java
            startActivity(Intent(this, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }, 900)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.tvError.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun validateInputs(): Boolean {
        var valid = true
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tvError.visibility = View.GONE

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) { binding.tilEmail.error = "Email is required"; valid = false }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"; valid = false
        }
        if (password.isEmpty()) { binding.tilPassword.error = "Password is required"; valid = false }
        return valid
    }
}
