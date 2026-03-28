package com.example.techloan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Check for saved token
        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        Handler(Looper.getMainLooper()).postDelayed({
            if (!token.isNullOrEmpty()) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}