package com.example.techloan.features.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.R
import com.example.techloan.databinding.ActivityProfileBinding
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.features.inventory.InventoryActivity
import com.example.techloan.features.penalty.MyPenaltiesActivity
import com.example.techloan.features.reservation.MyReservationsActivity
import com.example.techloan.shared.model.LoanDto
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.btnEdit.setOnClickListener { showEditDialog() }
        binding.btnChangePassword.setOnClickListener { showChangePasswordDialog() }

        val prefs  = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token  = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        val userId = prefs.getLong("user_id", 0L)

        viewModel.loadProfile(token, userId)
        observeViewModel()
        observeUpdateState()
        loadBorrowingHistory(token, userId)
        setupBottomNav()

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showEditDialog() {
        val currentUser = (viewModel.state.value as? ProfileState.Success)?.data?.user ?: return
        val density = resources.displayMetrics.density

        fun dp(v: Int) = (v * density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(4))
        }

        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(getColor(R.color.gray_400))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
        }

        fun field(hint: String, value: String?, inputType: Int = InputType.TYPE_CLASS_TEXT) =
            EditText(this).apply {
                this.hint = hint
                setText(value ?: "")
                this.inputType = inputType
                setTextColor(getColor(R.color.gray_900))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

        val etFullName = field("Full Name", currentUser.fullName,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        val etStudentId = field("Student / Faculty ID", currentUser.studentId)
        val etPersonalEmail = field("Personal Email", currentUser.personalEmail,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        container.apply {
            addView(label("Full Name"))
            addView(etFullName)
            addView(label("Student / Faculty ID"))
            addView(etStudentId)
            addView(label("Personal Email"))
            addView(etPersonalEmail)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(container)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val studentId = etStudentId.text.toString().trim().ifEmpty { null }
            val personalEmail = etPersonalEmail.text.toString().trim().ifEmpty { null }

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Full name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            val token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
            viewModel.updateProfile(token, fullName, studentId, personalEmail, null, null)
            dialog.dismiss()
        }
    }

    private fun showChangePasswordDialog() {
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(4))
        }

        fun label(text: String) = TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(getColor(R.color.gray_400))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(10) }
        }
        fun pwField(hint: String) = EditText(this).apply {
            this.hint = hint
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(getColor(R.color.gray_900))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val etCurrent = pwField("Current Password")
        val etNew = pwField("New Password")
        val etConfirm = pwField("Confirm New Password")

        container.apply {
            addView(label("Current Password"))
            addView(etCurrent)
            addView(label("New Password"))
            addView(etNew)
            addView(label("Confirm New Password"))
            addView(etConfirm)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(container)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val current = etCurrent.text.toString()
            val newPwd = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            when {
                current.isEmpty() -> Toast.makeText(this, "Enter your current password", Toast.LENGTH_SHORT).show()
                newPwd.length < 6 -> Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                newPwd != confirm -> Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                else -> {
                    val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
                    val token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
                    val currentUser = (viewModel.state.value as? ProfileState.Success)?.data?.user
                    viewModel.updateProfile(
                        token,
                        fullName = currentUser?.fullName ?: "",
                        studentId = currentUser?.studentId,
                        personalEmail = currentUser?.personalEmail,
                        currentPassword = current,
                        newPassword = newPwd
                    )
                    dialog.dismiss()
                }
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_profile
        binding.bottomNav.setOnItemSelectedListener { item ->
            fun go(cls: Class<*>) = startActivity(
                Intent(this, cls).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            )
            when (item.itemId) {
                R.id.nav_home         -> { go(DashboardActivity::class.java); true }
                R.id.nav_inventory    -> { go(InventoryActivity::class.java); true }
                R.id.nav_reservations -> { go(MyReservationsActivity::class.java); true }
                R.id.nav_penalties    -> { go(MyPenaltiesActivity::class.java); true }
                R.id.nav_profile      -> true
                else -> false
            }
        }
    }

    private fun observeUpdateState() {
        viewModel.updateState.observe(this) { state ->
            when (state) {
                is UpdateState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UpdateState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
                    prefs.edit().putString("user_name", state.user.fullName).apply()
                    viewModel.resetUpdateState()
                }
                is UpdateState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetUpdateState()
                }
                is UpdateState.Idle -> {}
            }
        }
    }

    private fun loadBorrowingHistory(token: String, userId: Long) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getMyLoans(token, userId)
                if (res.isSuccessful) {
                    val loans = (res.body() ?: emptyList()).take(5)
                    updateBorrowingHistory(loans)
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateBorrowingHistory(loans: List<LoanDto>) {
        binding.llHistory.removeAllViews()
        if (loans.isEmpty()) {
            binding.tvHistoryEmpty.visibility = View.VISIBLE
            return
        }
        binding.tvHistoryEmpty.visibility = View.GONE
        loans.forEachIndexed { index, loan ->
            if (index > 0) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply { setMargins(0, 10, 0, 10) }
                    setBackgroundColor(getColor(R.color.gray_100))
                }
                binding.llHistory.addView(divider)
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val statusColor = when (loan.status?.uppercase()) {
                "ACTIVE"     -> getColor(R.color.green_600)
                "RETURNED"   -> getColor(R.color.gray_400)
                "OVERDUE"    -> getColor(R.color.primary)
                else         -> getColor(R.color.gold_dark)
            }

            val dot = View(this).apply {
                val size = (8 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (10 * resources.displayMetrics.density).toInt()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(statusColor)
                }
            }
            row.addView(dot)

            val nameView = TextView(this).apply {
                text = loan.itemName ?: "Item"
                textSize = 13f
                setTextColor(getColor(R.color.gray_900))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(nameView)

            val statusView = TextView(this).apply {
                text = loan.status ?: "Unknown"
                textSize = 11f
                setTextColor(statusColor)
                typeface = android.graphics.Typeface.create("sans-serif-bold", android.graphics.Typeface.BOLD)
            }
            row.addView(statusView)

            binding.llHistory.addView(row)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ProfileState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
                is ProfileState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE

                    val user      = state.data.user
                    val penalties = state.data.penalties

                    val initials = user.fullName
                        ?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        ?.take(2)
                        ?.joinToString("") ?: "U"

                    binding.tvInitials.text     = initials
                    binding.tvName.text         = user.fullName  ?: "—"
                    binding.tvEmail.text        = user.email     ?: "—"
                    binding.tvRole.text         = user.role      ?: "—"

                    // Account details card
                    binding.tvNameDetail.text   = user.fullName  ?: "—"
                    binding.tvEmailDetail.text  = user.email     ?: "—"
                    binding.tvStudentId.text    = user.studentId ?: "—"
                    binding.tvRoleDetail.text   = user.role      ?: "—"

                    val isInstitutional = user.email?.endsWith("@cit.edu", ignoreCase = true) == true
                    binding.tvInstitutional.text = if (isInstitutional)
                        "CIT-U Institutional Account" else "Standard Account"

                    binding.tvPenaltyPoints.text = penalties.totalPoints.toString()

                    if (penalties.totalPoints > 0) {
                        binding.cardPenalty.setCardBackgroundColor(getColor(R.color.red_50))
                        binding.tvPenaltyPoints.setTextColor(getColor(R.color.red_400))
                    }
                }
                is ProfileState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    binding.tvName.text = state.message
                }
            }
        }
    }
}
