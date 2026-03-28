package com.example.techloan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val email = prefs.getString("user_email", "")

//        if (!email.isNullOrEmpty()) {
//            binding.tvUserEmail.text = email
//        }

        binding.toolbar.inflateMenu(R.menu.menu_dashboard)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) {
                logout()
                true
            } else false
        }

        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun logout() {
        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}