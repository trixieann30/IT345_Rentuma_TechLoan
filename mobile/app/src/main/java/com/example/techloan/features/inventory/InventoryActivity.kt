package com.example.techloan.features.inventory

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.R
import com.example.techloan.databinding.ActivityInventoryBinding
import com.example.techloan.features.dashboard.DashboardActivity
import com.example.techloan.features.penalty.MyPenaltiesActivity
import com.example.techloan.features.profile.ProfileActivity
import com.example.techloan.features.reservation.CreateReservationActivity
import com.example.techloan.features.reservation.MyReservationsActivity

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private val viewModel: InventoryViewModel by viewModels()
    private lateinit var adapter: InventoryAdapter
    private lateinit var token: String
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filter(s?.toString() ?: "", selectedCategory)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnGuide.setOnClickListener { showBorrowingGuide() }

        observeViewModel()
        viewModel.loadItems(token)
        setupBottomNav()

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("guide_seen", false)) {
            showBorrowingGuide()
        }
    }

    private fun showBorrowingGuide() {
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val scroll = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(8))
        }

        fun sectionTitle(text: String) = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(getColor(R.color.gray_900))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(14); bottomMargin = dp(4) }
        }

        fun bodyText(text: String) = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(getColor(R.color.gray_600))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(2) }
        }

        // Steps
        container.addView(sectionTitle("How to Borrow Equipment"))

        val steps = listOf(
            "1. Browse" to "Find the equipment you need from our inventory list.",
            "2. Reserve" to "Select the item and submit a reservation request with your preferred dates.",
            "3. Wait for Approval" to "The lab custodian will review and approve your request.",
            "4. Pick Up" to "Collect the equipment from the lab during your scheduled time.",
            "5. Return On Time" to "Return the equipment by the due date to avoid penalties."
        )
        steps.forEach { (title, desc) ->
            container.addView(bodyText("$title — $desc"))
        }

        // T&C
        container.addView(sectionTitle("Terms & Conditions"))

        val terms = listOf(
            "• For academic use only — equipment must be used for academic or research activities.",
            "• Handle with care — users are responsible for the equipment while in their possession.",
            "• 1 penalty point per day late, equivalent to ₱50 per point.",
            "• Accumulated penalty points may restrict your borrowing privileges.",
            "• Lost or damaged items must be reported immediately; borrower is liable for repair or replacement.",
            "• No personal, commercial, or off-campus use is permitted."
        )
        terms.forEach { container.addView(bodyText(it)) }

        scroll.addView(container)

        AlertDialog.Builder(this)
            .setTitle("Borrowing Guide")
            .setView(scroll)
            .setPositiveButton("I Understand — Let me Browse") { dialog, _ ->
                getSharedPreferences("techloan_prefs", MODE_PRIVATE).edit()
                    .putBoolean("guide_seen", true).apply()
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_inventory
        binding.bottomNav.setOnItemSelectedListener { item ->
            fun go(cls: Class<*>) = startActivity(
                Intent(this, cls).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
            )
            when (item.itemId) {
                R.id.nav_home         -> { go(DashboardActivity::class.java); true }
                R.id.nav_inventory    -> true
                R.id.nav_reservations -> { go(MyReservationsActivity::class.java); true }
                R.id.nav_penalties    -> { go(MyPenaltiesActivity::class.java); true }
                R.id.nav_profile      -> { go(ProfileActivity::class.java); true }
                else -> false
            }
        }
    }

    private fun buildCategoryChips(categories: List<String>) {
        binding.llCategories.removeAllViews()
        addChip("All", null)
        categories.forEach { addChip(it, it) }
    }

    private fun addChip(label: String, category: String?) {
        val chip = TextView(this).apply {
            text = label
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            val hPad = (12 * resources.displayMetrics.density).toInt()
            val vPad = (6 * resources.displayMetrics.density).toInt()
            setPadding(hPad, vPad, hPad, vPad)
            val marginEnd = (8 * resources.displayMetrics.density).toInt()
            val lp = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, marginEnd, 0) }
            layoutParams = lp
            setActive(category == selectedCategory)
        }
        chip.setOnClickListener {
            selectedCategory = category
            updateChipStates()
            viewModel.filter(binding.etSearch.text?.toString() ?: "", selectedCategory)
        }
        chip.tag = category ?: "all"
        binding.llCategories.addView(chip)
    }

    private fun updateChipStates() {
        for (i in 0 until binding.llCategories.childCount) {
            val chip = binding.llCategories.getChildAt(i) as? TextView ?: continue
            val chipCat = if (chip.tag == "all") null else chip.tag as? String
            chip.setActive(chipCat == selectedCategory)
        }
    }

    private fun TextView.setActive(active: Boolean) {
        if (active) {
            setBackgroundResource(R.drawable.bg_badge_red)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            setBackgroundResource(R.drawable.bg_chip_category)
            setTextColor(ContextCompat.getColor(context, R.color.gray_600))
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is InventoryState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvInventory.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
                is InventoryState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val cats = viewModel.categories()
                    if (binding.llCategories.childCount == 0 && cats.isNotEmpty()) {
                        buildCategoryChips(cats)
                    }
                    if (state.items.isEmpty()) {
                        binding.rvInventory.visibility = View.GONE
                        binding.tvEmpty.text = if (binding.etSearch.text?.isNotEmpty() == true || selectedCategory != null)
                            "No results found." else "No equipment available."
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvInventory.visibility = View.VISIBLE
                        adapter.updateItems(state.items)
                    }
                }
                is InventoryState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvInventory.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.layoutEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.imageUpdate.observe(this) { (id, url) ->
            adapter.updateImage(id, url)
        }
    }
}
