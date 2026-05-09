package com.example.techloan.features.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.databinding.ActivityLoginBinding
import com.example.techloan.features.custodian.CustodianDashboardActivity
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.shared.model.GoogleAuthRequestDto
import com.example.techloan.shared.network.RetrofitClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

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
                sendGoogleToken(idToken, "STUDENT")
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
                val email = binding.etEmail.text.toString().trim().lowercase()
                val password = binding.etPassword.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            binding.tvError.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            googleSignInClient.signOut().addOnCompleteListener {
                signInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun sendGoogleToken(idToken: String, role: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.googleSignIn(
                    GoogleAuthRequestDto(idToken = idToken, role = role)
                )
                binding.progressBar.visibility = View.GONE
                if (res.isSuccessful) {
                    val body = res.body()!!
                    saveSessionAndNavigate(
                        token = body.token ?: "",
                        userId = body.user?.id ?: 0L,
                        fullName = body.user?.fullName ?: "",
                        email = body.user?.email ?: "",
                        role = body.user?.role ?: ""
                    )
                } else {
                    showError("Sign-in failed (${res.code()}): check your Google account is @cit.edu")
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun saveSessionAndNavigate(
        token: String, userId: Long, fullName: String, email: String, role: String
    ) {
        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("jwt_token", token)
            .putLong("user_id", userId)
            .putString("user_name", fullName)
            .putString("user_email", email)
            .putString("user_role", role)
            .apply()

        binding.tvSuccess.text = "Sign-in successful!"
        binding.tvSuccess.visibility = View.VISIBLE

        binding.root.postDelayed({
            val target = if (role == "CUSTODIAN") CustodianDashboardActivity::class.java
                         else DashboardActivity::class.java
            val intent = Intent(this, target)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val email = binding.etEmail.text.toString().trim().lowercase()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tvError.visibility = View.GONE

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"; isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"; isValid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"; isValid = false
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
                    binding.tvSuccess.visibility = View.VISIBLE
                    binding.tvSuccess.text = state.message ?: "Login successful!"
                    binding.tvError.visibility = View.GONE

                    saveSessionAndNavigate(
                        token = state.token,
                        userId = state.user?.id ?: 0L,
                        fullName = state.user?.fullName ?: "",
                        email = state.user?.email ?: "",
                        role = state.user?.role ?: ""
                    )
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    showError(state.message)
                }
                is AuthState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }
}
