package com.example.techloan.features.custodian

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityAllLoansManagementBinding

class AllLoansManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllLoansManagementBinding
    private val viewModel: CustodianViewModel by viewModels()
    private lateinit var adapter: LoanManagementAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllLoansManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        token = "Bearer ${getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            .getString("jwt_token", "") ?: ""}"

        adapter = LoanManagementAdapter(
            onReturn = { loan -> viewModel.returnLoan(token, loan.id) }
        )

        binding.rvLoans.layoutManager = LinearLayoutManager(this)
        binding.rvLoans.adapter = adapter

        observeViewModel()
        viewModel.loadAllLoans(token)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.loanState.observe(this) { state ->
            when (state) {
                is CustodianState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvLoans.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is CustodianState.LoansLoaded -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvLoans.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvLoans.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is CustodianState.ActionSuccess -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.loadAllLoans(token)
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
