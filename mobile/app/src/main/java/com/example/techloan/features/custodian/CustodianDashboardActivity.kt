package com.example.techloan.features.custodian

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.databinding.ActivityCustodianDashboardBinding
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.shared.network.RetrofitClient
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class CustodianDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustodianDashboardBinding

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
        binding.tvWelcome.text = "Hi, ${name.split(" ").first()}"

        binding.btnPendingReservations.setOnClickListener {
            startActivity(Intent(this, PendingReservationsActivity::class.java))
        }
        binding.btnAllLoans.setOnClickListener {
            startActivity(Intent(this, AllLoansManagementActivity::class.java))
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
    }

    private fun loadStats() {
        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"

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
