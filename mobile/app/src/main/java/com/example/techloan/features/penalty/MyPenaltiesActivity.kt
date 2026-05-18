package com.example.techloan.features.penalty

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.R
import com.example.techloan.databinding.ActivityMyPenaltiesBinding
import com.example.techloan.shared.model.PenaltyDto

class MyPenaltiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPenaltiesBinding
    private val viewModel: MyPenaltiesViewModel by viewModels()
    private lateinit var adapter: PenaltyAdapter
    private lateinit var token: String
    private var userId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPenaltiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"
        userId = prefs.getLong("user_id", 0L)

        adapter = PenaltyAdapter { penalty -> viewModel.initiatePayment(token, penalty) }

        binding.rvPenalties.layoutManager = LinearLayoutManager(this)
        binding.rvPenalties.adapter = adapter

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        if (::token.isInitialized) viewModel.load(token, userId)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is PenaltyScreenState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                }
                is PenaltyScreenState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE

                    val totalPoints = state.summary.totalPoints
                    val penalties   = state.summary.penalties

                    binding.tvTotalPoints.text = totalPoints.toString()

                    if (totalPoints > 0) {
                        binding.summaryCard.setCardBackgroundColor(getColor(R.color.primary))
                        binding.tvSummaryDesc.text = "₱%,d due · payable via GCash or Maya".format(totalPoints * 50)
                    } else {
                        binding.summaryCard.setCardBackgroundColor(getColor(R.color.green_600))
                        binding.tvSummaryDesc.text = "All penalties cleared. Great job!"
                    }

                    if (penalties.isEmpty()) {
                        binding.rvPenalties.visibility = View.GONE
                        binding.emptyLayout.visibility = View.VISIBLE
                    } else {
                        binding.rvPenalties.visibility = View.VISIBLE
                        binding.emptyLayout.visibility = View.GONE
                        adapter.updateItems(penalties)
                    }
                }
                is PenaltyScreenState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.tvError.text = state.message
                }
            }
        }

        viewModel.paymentState.observe(this) { state ->
            when (state) {
                is PaymentInitState.Ready -> {
                    showPaymentDialog(state.penalty, state.checkoutUrl)
                    viewModel.resetPaymentState()
                }
                is PaymentInitState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetPaymentState()
                }
                else -> {}
            }
        }
    }

    private fun showPaymentDialog(penalty: PenaltyDto, checkoutUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Pay Penalty Fine")
            .setMessage(
                "Item: ${penalty.itemName ?: "Unknown"}\n" +
                "Days overdue: ${penalty.daysOverdue}\n" +
                "Penalty points: ${penalty.penaltyPoints} pts\n\n" +
                "Amount due: ₱%,d.00\n\n".format(penalty.penaltyPoints * 50) +
                "You will be redirected to a secure payment page. Pay via GCash or Maya."
            )
            .setPositiveButton("Proceed to Pay") { _, _ ->
                if (checkoutUrl.isNotEmpty()) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)))
                } else {
                    Toast.makeText(this, "Checkout URL unavailable", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
