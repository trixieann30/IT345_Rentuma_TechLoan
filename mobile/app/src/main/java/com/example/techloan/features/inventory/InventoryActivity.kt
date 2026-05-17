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
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Browse Equipment"

        token = "Bearer ${getSharedPreferences("techloan_prefs", MODE_PRIVATE).getString("jwt_token", "") ?: ""}"

        adapter = InventoryAdapter { item ->
            startActivity(Intent(this, CreateReservationActivity::class.java).apply {
                putExtra("item_id", item.id)
                putExtra("item_name", item.itemName)
                putExtra("item_max_qty", item.availableQuantity)
            })
        }

        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter

        observeViewModel()
        viewModel.loadItems(token)
    }

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
                        binding.rvInventory.visibility = View.GONE
                        binding.tvEmpty.text = "No equipment available."
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvInventory.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is InventoryState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvInventory.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.imageUpdate.observe(this) { (id, url) ->
            adapter.updateImage(id, url)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
