package com.example.techloan.features.reservation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.databinding.ActivityCreateReservationBinding
import com.example.techloan.shared.model.CreateBorrowRequestDto
import java.util.Calendar

class CreateReservationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateReservationBinding
    private val viewModel: ReservationViewModel by viewModels()
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Borrow Item"

        val itemId   = intent.getLongExtra("item_id", 0L)
        val itemName = intent.getStringExtra("item_name") ?: "Unknown"
        val maxQty   = intent.getIntExtra("item_max_qty", 1)

        binding.tvItemName.text = itemName
        binding.tvMaxQty.text = "Max available: $maxQty"

        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.tvSelectedDate.text = "Return date: $selectedDate"
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.minDate = tomorrow.timeInMillis }.show()
        }

        binding.btnSubmit.setOnClickListener {
            val qty     = binding.etQuantity.text.toString().trim().toIntOrNull()
            val purpose = binding.etPurpose.text.toString().trim()

            if (qty == null || qty < 1) { binding.etQuantity.error = "Enter a valid quantity"; return@setOnClickListener }
            if (qty > maxQty)           { binding.etQuantity.error = "Exceeds available quantity"; return@setOnClickListener }
            if (purpose.isEmpty())      { binding.etPurpose.error = "Purpose is required"; return@setOnClickListener }
            if (selectedDate.isEmpty()) { Toast.makeText(this, "Please select a return date", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
            val token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"

            viewModel.submitReservation(token, CreateBorrowRequestDto(
                inventoryId = itemId, quantity = qty, purpose = purpose, returnDate = selectedDate
            ))
        }

        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is ReservationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is ReservationState.SubmitSuccess -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, "Reservation submitted! Awaiting approval.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is ReservationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                }
            }
        }
    }
}
