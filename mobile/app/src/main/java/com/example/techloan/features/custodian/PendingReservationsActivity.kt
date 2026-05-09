package com.example.techloan.features.custodian

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityPendingReservationsBinding

class PendingReservationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingReservationsBinding
    private val viewModel: CustodianViewModel by viewModels()
    private lateinit var adapter: PendingReservationAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        token = "Bearer ${getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            .getString("jwt_token", "") ?: ""}"

        adapter = PendingReservationAdapter(
            onApprove = { item ->
                viewModel.approveReservation(token, item.id)
            },
            onReject = { item ->
                viewModel.rejectReservation(token, item.id)
            }
        )

        binding.rvReservations.layoutManager = LinearLayoutManager(this)
        binding.rvReservations.adapter = adapter

        observeViewModel()
        viewModel.loadPendingReservations(token)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.reservationState.observe(this) { state ->
            when (state) {
                is CustodianState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvReservations.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is CustodianState.ReservationsLoaded -> {
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
                is CustodianState.ActionSuccess -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.loadPendingReservations(token)
                }
                is CustodianState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }
}
