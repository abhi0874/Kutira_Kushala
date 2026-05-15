package com.mindmatrix.kutirakushala

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Session check - if already logged in, go to Dashboard
        if (auth.currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        val rootLayout = findViewById<View>(R.id.login_root)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editPassword = findViewById<TextInputEditText>(R.id.editPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val statusText = findViewById<MaterialTextView>(R.id.statusText)
        val loginProgress = findViewById<CircularProgressIndicator>(R.id.loginProgress)

        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                statusText.text = "Authenticating with Enterprise Cloud..."
                loginProgress.visibility = View.VISIBLE
                btnLogin.isEnabled = false
                btnRegister.isEnabled = false
                
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        } else {
                            statusText.text = "Authentication Failure: ${task.exception?.localizedMessage}"
                            loginProgress.visibility = View.GONE
                            btnLogin.isEnabled = true
                            btnRegister.isEnabled = true
                        }
                    }
            } else {
                Toast.makeText(this, "Valid credentials required", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
