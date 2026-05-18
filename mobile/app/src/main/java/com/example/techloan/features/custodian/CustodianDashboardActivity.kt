package com.example.techloan.features.custodian

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.databinding.ActivityCustodianDashboardBinding
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.features.notification.NotificationActivity
import com.example.techloan.shared.network.RetrofitClient
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class CustodianDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustodianDashboardBinding
    private lateinit var token: String

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        val content = result.contents ?: return@registerForActivityResult
        val intent = Intent(this, QRScanResultActivity::class.java)
        intent.putExtra("scan_result", content)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustodianDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val name = prefs.getString("user_name", "Custodian") ?: "Custodian"
        token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        binding.tvWelcome.text = "Hi, ${name.split(" ").first()}"

        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.btnPendingReservations.setOnClickListener {
            startActivity(Intent(this, PendingReservationsActivity::class.java))
        }
        binding.btnAllLoans.setOnClickListener {
            startActivity(Intent(this, AllLoansManagementActivity::class.java))
        }
        binding.btnManagePenalties.setOnClickListener {
            startActivity(Intent(this, CustodianPenaltyManagementActivity::class.java))
        }
        binding.btnScanQr.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan student's reservation QR code")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            scanLauncher.launch(options)
        }
        binding.btnLogout.setOnClickListener { logout() }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
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

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val reservationsRes = RetrofitClient.api.getMyReservations(token, status = "PENDING")
                if (reservationsRes.isSuccessful) {
                    binding.tvPendingCount.text = "${reservationsRes.body()?.size ?: 0}"
                }
            } catch (_: Exception) {}

            try {
                val loansRes = RetrofitClient.api.getMyLoans(token)
                if (loansRes.isSuccessful) {
                    binding.tvLoansCount.text = "${loansRes.body()?.size ?: 0}"
                }
            } catch (_: Exception) {}
        }
    }

    private fun logout() {
        getSharedPreferences("techloan_prefs", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
