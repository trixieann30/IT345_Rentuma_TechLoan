package com.example.techloan.features.auth

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.R
import com.example.techloan.databinding.ActivityRegisterBinding
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.shared.model.UserDto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var selectedRole = "STUDENT"

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                showGoogleRoleDialog(idToken)
            } else {
                showError("Google sign-up failed: no ID token received")
            }
        } catch (e: ApiException) {
            showError("Google sign-up failed (code ${e.statusCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("428918354170-05e2q5d7l3uvbqeqngtje2fibcnr5kap.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupRoleDropdown()
        setupTermsCheckbox()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupTermsCheckbox() {
        val fullText = "I have read and agree to the Terms and Conditions"
        val linkText = "Terms and Conditions"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf(linkText)
        val end = start + linkText.length

        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) { showTermsDialog() }
            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.color = getColor(R.color.primary)
                ds.isUnderlineText = true
            }
        }
        spannable.setSpan(clickable, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(getColor(R.color.primary)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.cbTerms.text = spannable
        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showTermsDialog() {
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(12), dp(20), dp(8))
        }

        fun sectionTitle(text: String) = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(getColor(R.color.gray_900))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12); bottomMargin = dp(4) }
        }

        fun bodyText(text: String) = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(getColor(R.color.gray_600))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(2) }
        }

        val sections = listOf(
            "1. Eligibility" to
                "Only registered CIT-U students and faculty with a valid @cit.edu email address may use TechLoan.",
            "2. Equipment Use" to
                "All borrowed equipment must be used for academic or research activities only. Lending equipment to third parties is strictly prohibited.",
            "3. Returns and Penalties" to
                "Equipment must be returned by the agreed due date. Overdue items incur 1 penalty point per day, equivalent to ₱50 per point. Unpaid penalties will restrict your borrowing privileges.",
            "4. Damage and Loss" to
                "Any damage or loss must be reported to the lab custodian immediately. The borrower is fully liable for the cost of repair or replacement.",
            "5. Account Responsibility" to
                "You are responsible for all activity under your account. Do not share your credentials with anyone.",
            "6. Policy Changes" to
                "TechLoan reserves the right to update these terms at any time. Continued use of the system constitutes acceptance of the revised terms."
        )

        sections.forEach { (title, body) ->
            container.addView(sectionTitle(title))
            container.addView(bodyText(body))
        }

        scroll.addView(container)

        AlertDialog.Builder(this)
            .setTitle("Terms and Conditions")
            .setView(scroll)
            .setPositiveButton("I Agree") { dialog, _ ->
                binding.cbTerms.isChecked = true
                binding.tvTermsError.visibility = View.GONE
                dialog.dismiss()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun setupRoleDropdown() {
        val roles = listOf("STUDENT", "FACULTY")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
        binding.actvRole.setText("STUDENT", false)
        selectedRole = "STUDENT"
        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            selectedRole = roles[position]
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val fullName = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim().lowercase()
                val studentId = binding.etStudentId.text.toString().trim()
                val password = binding.etPassword.text.toString()
                val confirmPassword = binding.etConfirmPassword.text.toString()
                viewModel.register(fullName, email, studentId, password, confirmPassword, selectedRole)
            }
        }

        binding.btnGoogleSignUp.setOnClickListener {
            hideError()
            googleSignInClient.signOut().addOnCompleteListener {
                signInLauncher.launch(googleSignInClient.signInIntent)
            }
        }

        binding.btnGoToLogin.setOnClickListener { finish() }
        binding.btnGoToLoginFromVerification.setOnClickListener { finish() }
    }

    private fun showGoogleRoleDialog(idToken: String) {
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(12), dp(24), dp(8))
        }

        val tvLabel = TextView(this).apply {
            text = "CIT-U Institutional Email"
            textSize = 12f
            setTextColor(getColor(R.color.gray_700))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(6) }
        }

        val etCitEmail = EditText(this).apply {
            hint = "you@cit.edu"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setTextColor(getColor(R.color.gray_900))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }

        val tvRoleLabel = TextView(this).apply {
            text = "Account Type"
            textSize = 12f
            setTextColor(getColor(R.color.gray_700))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }

        val radioGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val rbStudent = RadioButton(this).apply {
            text = "STUDENT"
            isChecked = true
            setTextColor(getColor(R.color.gray_900))
            layoutParams = RadioGroup.LayoutParams(0, RadioGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val rbFaculty = RadioButton(this).apply {
            text = "FACULTY"
            setTextColor(getColor(R.color.gray_900))
            layoutParams = RadioGroup.LayoutParams(0, RadioGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        radioGroup.addView(rbStudent)
        radioGroup.addView(rbFaculty)

        val tvNote = TextView(this).apply {
            text = "This CIT-U email will be verified before you can borrow equipment."
            textSize = 11f
            setTextColor(getColor(R.color.gray_400))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(12) }
        }

        container.apply {
            addView(tvLabel)
            addView(etCitEmail)
            addView(tvRoleLabel)
            addView(radioGroup)
            addView(tvNote)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Complete Sign Up")
            .setView(container)
            .setPositiveButton("Continue", null)
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val citEmail = etCitEmail.text.toString().trim().lowercase()
            val role = if (rbFaculty.isChecked) "FACULTY" else "STUDENT"

            when {
                citEmail.isEmpty() ->
                    Toast.makeText(this, "Enter your CIT-U email", Toast.LENGTH_SHORT).show()
                !citEmail.endsWith("@cit.edu") ->
                    Toast.makeText(this, "Must be a @cit.edu email address", Toast.LENGTH_SHORT).show()
                else -> {
                    dialog.dismiss()
                    showLoading()
                    viewModel.registerWithGoogle(idToken, role, citEmail)
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val fullName = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        binding.tilName.error = null
        binding.tilEmail.error = null
        binding.tilStudentId.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tvTermsError.visibility = View.GONE
        hideError()

        if (fullName.length < 2) {
            binding.tilName.error = "Full name must be at least 2 characters"
            isValid = false
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        } else if (!email.lowercase().endsWith("@cit.edu")) {
            binding.tilEmail.error = "Must be a CIT-U email (@cit.edu)"
            isValid = false
        }

        if (studentId.isEmpty()) {
            binding.tilStudentId.error = "Student / Faculty ID is required"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        if (!binding.cbTerms.isChecked) {
            binding.tvTermsError.visibility = View.VISIBLE
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading()
                is AuthState.Success -> {
                    hideLoading()
                    when {
                        state.token.isNotEmpty() && state.user != null ->
                            saveSessionAndNavigate(state.token, state.user)
                        state.token.isNotEmpty() -> {
                            // Token returned but no user object — go to login
                            binding.tvSuccess.text = "Account created! Please sign in."
                            binding.tvSuccess.visibility = View.VISIBLE
                            binding.root.postDelayed({ finish() }, 1500)
                        }
                        else -> showVerificationScreen()
                    }
                }
                is AuthState.Error -> {
                    hideLoading()
                    showError(state.message)
                }
                is AuthState.Idle -> hideLoading()
            }
        }
    }

    private fun showVerificationScreen() {
        binding.layoutForm.visibility = View.GONE
        binding.layoutVerification.visibility = View.VISIBLE
        binding.btnGoToLogin.visibility = View.GONE
    }

    private fun saveSessionAndNavigate(token: String, user: UserDto) {
        getSharedPreferences("techloan_prefs", MODE_PRIVATE).edit()
            .putString("jwt_token", token)
            .putLong("user_id", user.id ?: 0L)
            .putString("user_name", user.fullName ?: "")
            .putString("user_email", user.email ?: "")
            .putString("user_role", user.role ?: "")
            .apply()

        binding.tvSuccess.text = "Account created! Redirecting…"
        binding.tvSuccess.visibility = View.VISIBLE

        binding.root.postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }, 1200)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
        binding.btnGoogleSignUp.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
        binding.btnGoogleSignUp.isEnabled = true
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun hideError() {
        binding.tvError.visibility = View.GONE
    }
}
