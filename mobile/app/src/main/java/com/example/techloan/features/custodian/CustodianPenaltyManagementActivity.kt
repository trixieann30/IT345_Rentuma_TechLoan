package com.example.techloan.features.custodian

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.R
import com.example.techloan.databinding.ActivityCustodianPenaltyManagementBinding
import com.google.android.material.chip.Chip

class CustodianPenaltyManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustodianPenaltyManagementBinding
    private val viewModel: CustodianPenaltyViewModel by viewModels()
    private lateinit var adapter: CustodianPenaltyAdapter
    private lateinit var token: String
    private var activeFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustodianPenaltyManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"

        adapter = CustodianPenaltyAdapter()
        binding.rvPenalties.layoutManager = LinearLayoutManager(this)
        binding.rvPenalties.adapter = adapter

        binding.chipAll.setOnClickListener { setFilter("ALL") }
        binding.chipUnpaid.setOnClickListener { setFilter("UNPAID") }
        binding.chipPaid.setOnClickListener { setFilter("PAID") }

        observeViewModel()
        viewModel.load(token)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun setFilter(filter: String) {
        activeFilter = filter
        updateChipStyles()
        viewModel.filter(filter)
    }

    private fun updateChipStyles() {
        val chips = mapOf("ALL" to binding.chipAll, "UNPAID" to binding.chipUnpaid, "PAID" to binding.chipPaid)
        chips.forEach { (key, chip) ->
            if (key == activeFilter) {
                chip.setChipBackgroundColorResource(R.color.primary)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setChipBackgroundColorResource(R.color.gray_100)
                chip.setTextColor(ContextCompat.getColor(this, R.color.gray_600))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AdminPenaltyState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPenalties.visibility = View.GONE
                    binding.emptyLayout.visibility = View.GONE
                    binding.filterRow.visibility = View.GONE
                }
                is AdminPenaltyState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.filterRow.visibility = View.VISIBLE
                    if (state.items.isEmpty()) {
                        binding.rvPenalties.visibility = View.GONE
                        binding.emptyLayout.visibility = View.VISIBLE
                    } else {
                        binding.rvPenalties.visibility = View.VISIBLE
                        binding.emptyLayout.visibility = View.GONE
                        adapter.updateItems(state.items)
                    }
                }
                is AdminPenaltyState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.filterRow.visibility = View.GONE
                    binding.rvPenalties.visibility = View.GONE
                    binding.emptyLayout.visibility = View.VISIBLE
                    binding.tvEmpty.text = state.message
                }
            }
        }
    }
}
