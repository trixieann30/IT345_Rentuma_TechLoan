package com.example.techloan.features.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityMyReservationsBinding
import com.example.techloan.features.reservation.QRCodeViewActivity

class MyReservationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyReservationsBinding
    private val viewModel: ReservationViewModel by viewModels()
    private lateinit var adapter: ReservationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Reservations"

        adapter = ReservationAdapter(onShowQr = { reservation ->
            val intent = Intent(this, QRCodeViewActivity::class.java)
            intent.putExtra("reservation_id", reservation.id)
            startActivity(intent)
        })
        binding.rvReservations.layoutManager = LinearLayoutManager(this)
        binding.rvReservations.adapter = adapter

        val prefs  = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token  = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        val userId = prefs.getLong("user_id", 0L)

        viewModel.loadMyReservations(token, userId)
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ReservationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvReservations.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is ReservationState.ListSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvReservations.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvReservations.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is ReservationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }
}
