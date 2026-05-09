package com.example.techloan.features.inventory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityInventoryBinding
import com.example.techloan.features.reservation.CreateReservationActivity

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Browse Inventory"

        adapter = InventoryAdapter { item ->
            val intent = Intent(this, CreateReservationActivity::class.java).apply {
                putExtra("item_id", item.id)
                putExtra("item_name", item.itemName)
                putExtra("item_max_qty", item.availableQuantity)
            }
            startActivity(intent)
        }

        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        viewModel.loadItems(token)

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is InventoryState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvInventory.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is InventoryState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvInventory.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvInventory.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is InventoryState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }
    }
}
