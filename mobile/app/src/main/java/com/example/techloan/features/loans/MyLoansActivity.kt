package com.example.techloan.features.loans

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityMyLoansBinding

class MyLoansActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyLoansBinding
    private val viewModel: LoanViewModel by viewModels()
    private lateinit var adapter: LoanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLoansBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Loans"

        adapter = LoanAdapter()
        binding.rvLoans.layoutManager = LinearLayoutManager(this)
        binding.rvLoans.adapter = adapter

        val prefs  = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token  = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        val userId = prefs.getLong("user_id", 0L)

        viewModel.loadMyLoans(token, userId)
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is LoanState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvLoans.visibility = View.GONE
                    binding.emptyLayout.visibility = View.GONE
                }
                is LoanState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.loans.isEmpty()) {
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.rvLoans.visibility = View.GONE
                    } else {
                        binding.emptyLayout.visibility = View.GONE
                        binding.rvLoans.visibility = View.VISIBLE
                        adapter.updateLoans(state.loans)
                    }
                }
                is LoanState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.emptyLayout.visibility = View.VISIBLE
                }
            }
        }
    }
}
