package com.example.techloan.features.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.R
import com.example.techloan.databinding.ActivityProfileBinding
import com.example.techloan.features.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        val prefs  = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token  = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        val userId = prefs.getLong("user_id", 0L)

        viewModel.loadProfile(token, userId)
        observeViewModel()

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

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

                    binding.tvName.text      = user.fullName  ?: "—"
                    binding.tvEmail.text     = user.email     ?: "—"
                    binding.tvStudentId.text = user.studentId ?: "—"
                    binding.tvRole.text      = user.role      ?: "—"
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
