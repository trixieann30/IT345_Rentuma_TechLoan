package com.example.techloan.features.notification

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.techloan.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notifications"

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        token = "Bearer ${prefs.getString("jwt_token", "") ?: ""}"

        adapter = NotificationAdapter(onItemClick = { notification ->
            if (!notification.read) {
                viewModel.markRead(token, notification.id)
            }
        })

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter

        binding.btnMarkAllRead.setOnClickListener {
            viewModel.markAllRead(token)
        }

        viewModel.state.observe(this) { state ->
            when (state) {
                is NotificationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is NotificationState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.items.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE
                        binding.btnMarkAllRead.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE
                        binding.btnMarkAllRead.visibility =
                            if (state.items.any { !it.read }) View.VISIBLE else View.GONE
                        adapter.updateItems(state.items)
                    }
                }
                is NotificationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.text = state.message
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        viewModel.load(token)
        // Auto-mark all as read when notification screen opens (mirrors web bell behaviour)
        viewModel.markAllRead(token)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
