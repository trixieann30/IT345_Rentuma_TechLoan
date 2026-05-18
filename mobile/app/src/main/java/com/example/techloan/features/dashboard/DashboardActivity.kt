package com.example.techloan.features.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.R
import com.example.techloan.databinding.ActivityDashboardBinding
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.features.inventory.InventoryActivity
import com.example.techloan.features.loans.MyLoansActivity
import com.example.techloan.features.notification.NotificationActivity
import com.example.techloan.features.penalty.MyPenaltiesActivity
import com.example.techloan.features.profile.ProfileActivity
import com.example.techloan.features.reservation.MyReservationsActivity
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "User") ?: "User"
        token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        binding.tvWelcome.text = "👋 Hello, ${userName.split(" ").first()}!"

        binding.toolbar.inflateMenu(R.menu.menu_dashboard)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_logout) { logout(); true } else false
        }

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.btnLogout.setOnClickListener { logout() }

        binding.cardBrowse.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }
        binding.cardReservations.setOnClickListener {
            startActivity(Intent(this, MyReservationsActivity::class.java))
        }
        binding.cardLoans.setOnClickListener {
            startActivity(Intent(this, MyLoansActivity::class.java))
        }
        binding.cardPenalties.setOnClickListener {
            startActivity(Intent(this, MyPenaltiesActivity::class.java))
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home      -> true
                R.id.nav_inventory -> { startActivity(Intent(this, InventoryActivity::class.java)); true }
                R.id.nav_loans     -> { startActivity(Intent(this, MyLoansActivity::class.java)); true }
                R.id.nav_profile   -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecentActivity()
        loadUnreadCount()
    }

    private fun loadUnreadCount() {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getUnreadCount(token)
                if (res.isSuccessful) {
                    val count = res.body()?.count ?: 0L
                    if (count > 0L) {
                        binding.tvNotifBadge.text = if (count > 9) "9+" else count.toString()
                        binding.tvNotifBadge.visibility = View.VISIBLE
                    } else {
                        binding.tvNotifBadge.visibility = View.GONE
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadRecentActivity() {
        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val userId = prefs.getLong("user_id", 0L)
        if (userId == 0L) return

        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getMyReservations(token, userId)
                if (res.isSuccessful) {
                    val items = res.body() ?: emptyList()
                    if (items.isEmpty()) {
                        binding.tvActivity1.text = "No recent activity"
                        binding.cardActivity2.visibility = View.GONE
                    } else {
                        val first = items.first()
                        binding.tvActivity1.text = "${first.itemName ?: "Item"} — ${first.status ?: "Pending"}"
                        if (items.size >= 2) {
                            val second = items[1]
                            binding.tvActivity2.text = "${second.itemName ?: "Item"} — ${second.status ?: "Pending"}"
                            binding.cardActivity2.visibility = View.VISIBLE
                        } else {
                            binding.cardActivity2.visibility = View.GONE
                        }
                    }
                }
            } catch (_: Exception) {}
        }
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
