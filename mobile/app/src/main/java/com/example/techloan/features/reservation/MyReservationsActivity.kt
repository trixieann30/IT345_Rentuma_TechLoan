package com.example.techloan.features.reservation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.R
import com.example.techloan.databinding.ActivityMyReservationsBinding
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.features.inventory.InventoryActivity
import com.example.techloan.features.penalty.MyPenaltiesActivity
import com.example.techloan.features.profile.ProfileActivity
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
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

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
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_reservations
        binding.bottomNav.setOnItemSelectedListener { item ->
            fun go(cls: Class<*>) = startActivity(
                Intent(this, cls).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            )
            when (item.itemId) {
                R.id.nav_home         -> { go(DashboardActivity::class.java); true }
                R.id.nav_inventory    -> { go(InventoryActivity::class.java); true }
                R.id.nav_reservations -> true
                R.id.nav_penalties    -> { go(MyPenaltiesActivity::class.java); true }
                R.id.nav_profile      -> { go(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }


    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ReservationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvReservations.visibility = View.GONE
                    binding.emptyLayout.visibility = View.GONE
                }
                is ReservationState.ListSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.items.isEmpty()) {
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.rvReservations.visibility = View.GONE
                    } else {
                        binding.emptyLayout.visibility = View.GONE
                        binding.rvReservations.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is ReservationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.emptyLayout.visibility = View.VISIBLE
                }
                else -> {}
            }
        }
    }
}
