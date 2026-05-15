package com.mindmatrix.kutirakushala

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
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
        setContentView(R.layout.activity_main)
        
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onBackPressedDispatcher.onBackPressed() 
        }

        val root = findViewById<View>(R.id.main_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        val layoutName = findViewById<TextInputLayout>(R.id.layoutName)
        val layoutQty = findViewById<TextInputLayout>(R.id.layoutQty)
        val layoutPrice = findViewById<TextInputLayout>(R.id.layoutPrice)
        
        val editName = findViewById<TextInputEditText>(R.id.editName)
        val editQty = findViewById<TextInputEditText>(R.id.editQty)
        val editPrice = findViewById<TextInputEditText>(R.id.editPrice)
        val editDescription = findViewById<TextInputEditText>(R.id.editDescription)
        
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnExport = findViewById<MaterialButton>(R.id.btnExport)
        val statusText = findViewById<MaterialTextView>(R.id.statusText)
        val syncProgress = findViewById<LinearProgressIndicator>(R.id.syncProgress)

        btnSave.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val name = editName.text.toString().trim()
            val qtyStr = editQty.text.toString().trim()
            val priceStr = editPrice.text.toString().trim()
            val description = editDescription.text.toString().trim()

            layoutName.error = null
            layoutQty.error = null
            layoutPrice.error = null

            var isValid = true
            if (name.isEmpty()) {
                layoutName.error = getString(R.string.error_field_mandatory)
                isValid = false
            }
            if (qtyStr.isEmpty()) {
                layoutQty.error = getString(R.string.error_field_mandatory)
                isValid = false
            }
            if (priceStr.isEmpty()) {
                layoutPrice.error = getString(R.string.error_field_mandatory)
                isValid = false
            }

            if (isValid) {
                statusText.text = getString(R.string.syncing_cloud)
                syncProgress.visibility = View.VISIBLE
                btnSave.isEnabled = false
                
                val item: MutableMap<String, Any> = HashMap()
                item["name"] = name
                item["quantity"] = qtyStr.toIntOrNull() ?: 0
                item["price_per_unit"] = priceStr.toDoubleOrNull() ?: 0.0
                item["description"] = description
                item["manager"] = auth.currentUser?.email ?: "unknown"
                item["timestamp"] = com.google.firebase.Timestamp.now()

                db.collection("inventory")
                    .add(item)
                    .addOnSuccessListener {
                        statusText.text = getString(R.string.sync_success)
                        syncProgress.visibility = View.GONE
                        
                        editName.text?.clear()
                        editQty.text?.clear()
                        editPrice.text?.clear()
                        editDescription.text?.clear()
                        
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(btnSave.windowToken, 0)
                        
                        Snackbar.make(root, "Inventory update synchronized", Snackbar.LENGTH_SHORT)
                            .setAnchorView(R.id.bottomNavigation)
                            .show()
                        
                        btnSave.isEnabled = true
                    }
                    .addOnFailureListener { e ->
                        statusText.text = getString(R.string.sync_error, e.localizedMessage)
                        syncProgress.visibility = View.GONE
                        btnSave.isEnabled = true
                    }
            }
        }

        btnExport.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            Snackbar.make(root, "Exporting stock manifest...", Snackbar.LENGTH_SHORT)
                .setAnchorView(R.id.bottomNavigation)
                .show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_inventory
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_inventory -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
