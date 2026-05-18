package com.example.techloan

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.features.dashboard.DashboardActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val root = findViewById<View>(R.id.rootLayout)
        root.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }
        val cardLogo = findViewById<View>(R.id.cardLogo)
        cardLogo.scaleX = 0.7f
        cardLogo.scaleY = 0.7f
        val scaleX = ObjectAnimator.ofFloat(cardLogo, "scaleX", 0.7f, 1f).apply { duration = 700; interpolator = DecelerateInterpolator() }
        val scaleY = ObjectAnimator.ofFloat(cardLogo, "scaleY", 0.7f, 1f).apply { duration = 700; interpolator = DecelerateInterpolator() }
        AnimatorSet().apply { playTogether(fadeIn, scaleX, scaleY); start() }

        val prefs = getSharedPreferences("techloan_prefs", MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (!token.isNullOrEmpty()) {
                Intent(this, DashboardActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 1800)
    }
}