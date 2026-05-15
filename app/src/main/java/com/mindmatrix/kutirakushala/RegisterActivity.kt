package com.mindmatrix.kutirakushala

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { 
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish() 
        }

        val root = findViewById<View>(R.id.register_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val layoutEmail = findViewById<TextInputLayout>(R.id.layoutEmail)
        val layoutPassword = findViewById<TextInputLayout>(R.id.layoutPassword)
        val layoutConfirmPassword = findViewById<TextInputLayout>(R.id.layoutConfirmPassword)

        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val editConfirmPassword = findViewById<TextInputEditText>(R.id.editConfirmPassword)
        val btnCreateAccount = findViewById<MaterialButton>(R.id.btnCreateAccount)
        val btnBackToLogin = findViewById<MaterialButton>(R.id.btnBackToLogin)
        val statusText = findViewById<MaterialTextView>(R.id.statusText)

        btnCreateAccount.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val confirmPassword = editConfirmPassword.text.toString().trim()

            layoutEmail.error = null
            layoutPassword.error = null
            layoutConfirmPassword.error = null

            var isValid = true

            if (email.isEmpty()) {
                layoutEmail.error = "Email address is required"
                isValid = false
            }
            if (password.isEmpty()) {
                layoutPassword.error = "Password is required"
                isValid = false
            }
            if (confirmPassword.isEmpty()) {
                layoutConfirmPassword.error = "Please confirm your password"
                isValid = false
            } else if (password != confirmPassword) {
                layoutConfirmPassword.error = "Security credentials do not match"
                isValid = false
            }

            if (password.length < 6 && password.isNotEmpty()) {
                layoutPassword.error = "Security protocol requires 6+ characters"
                isValid = false
            }

            if (isValid) {
                statusText.text = "Provisioning new enterprise account..."
                btnCreateAccount.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Snackbar.make(root, "Account Enrollment Successful", Snackbar.LENGTH_SHORT).show()
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finishAffinity()
                        } else {
                            statusText.text = "Enrollment Error: ${task.exception?.localizedMessage}"
                            btnCreateAccount.isEnabled = true
                            Snackbar.make(root, "Enrollment Failed: ${task.exception?.localizedMessage}", Snackbar.LENGTH_LONG).show()
                        }
                    }
            }
        }

        btnBackToLogin.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            finish()
        }
    }
}
