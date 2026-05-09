package com.example.techloan.features.reservation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.techloan.databinding.ActivityQrViewBinding
import com.example.techloan.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class QRCodeViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val reservationId = intent.getLongExtra("reservation_id", 0L)
        val token = "Bearer ${getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            .getString("jwt_token", "") ?: ""}"

        loadQrCode(token, reservationId)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun loadQrCode(token: String, id: Long) {
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getReservationQr(token, id)
                if (res.isSuccessful) {
                    val bytes = res.body()!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.progressBar.visibility = View.GONE
                    binding.ivQrCode.setImageBitmap(bitmap)
                    binding.ivQrCode.visibility = View.VISIBLE
                    binding.tvInstruction.visibility = View.VISIBLE
                } else {
                    showError("Failed to load QR code (${res.code()})")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun showError(msg: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }
}
