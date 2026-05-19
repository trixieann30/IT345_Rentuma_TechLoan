package com.example.techloan

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.techloan.features.auth.LoginActivity
import com.example.techloan.features.dashboard.DashboardActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val cardLogo   = findViewById<View>(R.id.cardLogo)
        val tvAppName  = findViewById<View>(R.id.tvAppName)
        val tvSubtitle = findViewById<View>(R.id.tvSubtitle)
        val accentLine = findViewById<View>(R.id.accentLine)
        val tvTagline  = findViewById<View>(R.id.tvTagline)
        val progressBar = findViewById<View>(R.id.progressBar)

        // Start invisible
        cardLogo.alpha = 0f; cardLogo.scaleX = 0.4f; cardLogo.scaleY = 0.4f
        tvAppName.alpha = 0f; tvAppName.translationY = 40f
        tvSubtitle.alpha = 0f
        accentLine.alpha = 0f; accentLine.scaleX = 0f
        tvTagline.alpha = 0f
        progressBar.alpha = 0f

        // Phase 1: Logo pops in with overshoot (0ms)
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(cardLogo, "scaleX", 0.4f, 1.08f, 1f),
                ObjectAnimator.ofFloat(cardLogo, "scaleY", 0.4f, 1.08f, 1f),
                ObjectAnimator.ofFloat(cardLogo, "alpha", 0f, 1f)
            )
            duration = 550
            interpolator = OvershootInterpolator(1.6f)
            start()
        }

        // Phase 2: App name slides up + fades in (350ms delay)
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(tvAppName, "translationY", 40f, 0f)
            )
            duration = 450
            startDelay = 350
            interpolator = DecelerateInterpolator()
            start()
        }

        // Phase 3: Subtitle fades in (550ms delay)
        ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 0.6f).apply {
            duration = 350; startDelay = 550; interpolator = DecelerateInterpolator(); start()
        }

        // Phase 4: Accent line grows from center (680ms delay)
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(accentLine, "scaleX", 0f, 1f),
                ObjectAnimator.ofFloat(accentLine, "alpha", 0f, 1f)
            )
            duration = 300
            startDelay = 680
            interpolator = DecelerateInterpolator()
            start()
        }

        // Phase 5: Tagline fades in (830ms delay)
        ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 0.4f).apply {
            duration = 300; startDelay = 830; interpolator = DecelerateInterpolator(); start()
        }

        // Phase 6: Progress bar fades in (1000ms delay)
        ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 0.5f).apply {
            duration = 300; startDelay = 1000; start()
        }

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
        }, 2000)
    }
}
