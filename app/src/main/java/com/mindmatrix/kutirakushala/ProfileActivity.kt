package com.mindmatrix.kutirakushala

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.textUserEmail).text = auth.currentUser?.email ?: "N/A"

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            showLogoutConfirmation()
        }

        findViewById<MaterialButton>(R.id.btnResetPassword).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val email = auth.currentUser?.email
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                Snackbar.make(root, getString(R.string.update_creds) + " sent to $email", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.bottomNavigation)
                    .show()
            }
        }

        findViewById<MaterialButton>(R.id.btnAppPrefs).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Snackbar.make(root, "Enterprise customization module offline", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.bottomNavigation)
                .show()
        }

        findViewById<MaterialButton>(R.id.btnSupport).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Snackbar.make(root, "Establishing secure connection to support...", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.bottomNavigation)
                .show()
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_profile
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_inventory -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sign_out)
            .setMessage(R.string.sign_out_confirm)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.confirm) { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .show()
    }
}
