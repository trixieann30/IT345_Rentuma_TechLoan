package com.example.techloan.features.custodian

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.databinding.ActivityQrScanResultBinding
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class QRScanResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScanResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val scanResult = intent.getStringExtra("scan_result") ?: ""
        val reservationId = parseReservationId(scanResult)

        if (reservationId == null) {
            showError("Invalid QR code: $scanResult")
            return
        }

        val token = "Bearer ${getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            .getString("jwt_token", "") ?: ""}"

        loadReservation(token, reservationId)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun parseReservationId(content: String): Long? =
        if (content.startsWith("TECHLOAN-RESERVATION-"))
            content.removePrefix("TECHLOAN-RESERVATION-").toLongOrNull()
        else null

    private fun loadReservation(token: String, id: Long) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getReservationById(token, id)
                if (res.isSuccessful) {
                    val r = res.body()!!
                    binding.progressBar.visibility = View.GONE
                    binding.tvItemName.text = r.itemName ?: "Unknown Item"
                    binding.tvStatus.text = r.status ?: "PENDING"
                    binding.tvBorrower.text = "Borrower: ${r.userEmail ?: "—"}"
                    binding.tvQuantity.text = "Quantity: ${r.quantity}"
                    binding.tvPurpose.text = "Purpose: ${r.purpose ?: "—"}"
                    binding.tvReturnDate.text = "Return by: ${r.returnDate ?: r.dueDate ?: "—"}"
                    binding.contentLayout.visibility = View.VISIBLE

                    if (r.status == "PENDING") {
                        binding.actionButtons.visibility = View.VISIBLE
                        binding.btnApprove.setOnClickListener { approve(token, id) }
                        binding.btnReject.setOnClickListener { reject(token, id) }
                    }
                } else {
                    showError("Reservation not found (${res.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun approve(token: String, id: Long) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.approveReservation(token, id)
                if (res.isSuccessful) {
                    binding.tvStatus.text = "APPROVED"
                    binding.actionButtons.visibility = View.GONE
                    Toast.makeText(this@QRScanResultActivity, "Reservation approved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@QRScanResultActivity, "Failed to approve (${res.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QRScanResultActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reject(token: String, id: Long) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.rejectReservation(token, id)
                if (res.isSuccessful) {
                    binding.tvStatus.text = "REJECTED"
                    binding.actionButtons.visibility = View.GONE
                    Toast.makeText(this@QRScanResultActivity, "Reservation rejected.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@QRScanResultActivity, "Failed to reject (${res.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@QRScanResultActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }
}
