package com.mindmatrix.kutirakushala

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val root = findViewById<View>(R.id.dashboard_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDynamicContent()
        setupInteractions()
        fetchLiveMetrics()
        fetchRecentActivity()
        renderInventoryChart()
    }

    private fun setupDynamicContent() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        findViewById<TextView>(R.id.txtWelcome).text = greeting

        val userEmail = auth.currentUser?.email ?: "Manager"
        val userName = userEmail.substringBefore("@")
            .split(".")
            .joinToString(" ") { segment -> 
                segment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } 
            }
//        findViewById<TextView>(R.id.txtUserName).text = userName
    }

    private fun setupInteractions() {
//        val searchView = findViewById<EditText>(R.id.editSearch)
//        searchView?.setOnEditorActionListener { v, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//                val query = v.text.toString()
//                if (query.isNotEmpty()) {
//                    Toast.makeText(this, "Querying enterprise database for: $query", Toast.LENGTH_SHORT).show()
//                }
//                true
//            } else false
//        }

        findViewById<FloatingActionButton>(R.id.fabQuickAdd).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            startActivity(Intent(this, MainActivity::class.java))
        }

        val clickListener = View.OnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            when (view.id) {
                R.id.cardArtisans -> startActivity(Intent(this, ArtisanActivity::class.java))
                R.id.cardInventory -> startActivity(Intent(this, MainActivity::class.java))
                R.id.cardProducts -> startActivity(Intent(this, ProductActivity::class.java))
                R.id.cardBuyers -> startActivity(Intent(this, BuyerActivity::class.java))
            }
        }

        findViewById<MaterialCardView>(R.id.cardArtisans).setOnClickListener(clickListener)
        findViewById<MaterialCardView>(R.id.cardInventory).setOnClickListener(clickListener)
        findViewById<MaterialCardView>(R.id.cardProducts).setOnClickListener(clickListener)
        findViewById<MaterialCardView>(R.id.cardBuyers).setOnClickListener(clickListener)

        findViewById<View>(R.id.imgNotifications).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Toast.makeText(this, "System Status: All protocols operational", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_inventory -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    false 
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun fetchLiveMetrics() {
        db.collection("inventory").get().addOnSuccessListener { result ->
            findViewById<TextView>(R.id.txtActiveTasks).text = result.size().toString()
            
            var totalValuation = 0.0
            for (doc in result) {
                val qty = doc.getLong("quantity") ?: 0
                val price = doc.getDouble("price_per_unit") ?: 0.0
                totalValuation += qty * price
            }
            
            val valuationStr = if (totalValuation >= 1000) {
                String.format(Locale.US, "₹%.1fk", totalValuation / 1000.0)
            } else {
                String.format(Locale.US, "₹%.0f", totalValuation)
            }
            findViewById<TextView>(R.id.txtSales).text = valuationStr
        }
    }

    private fun fetchRecentActivity() {
        db.collection("inventory")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(2)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener
                
                val docs = result.documents
                if (docs.isNotEmpty()) {
                    findViewById<TextView>(R.id.txtUpdateTitle1).text = "Stock: ${docs[0].getString("name")}"
                    findViewById<TextView>(R.id.txtUpdateDesc1).text = "Manager: ${docs[0].getString("manager")?.substringBefore("@")}"
                }
                if (docs.size > 1) {
                    findViewById<TextView>(R.id.txtUpdateTitle2).text = "Stock: ${docs[1].getString("name")}"
                    findViewById<TextView>(R.id.txtUpdateDesc2).text = "Manager: ${docs[1].getString("manager")?.substringBefore("@")}"
                }
            }
    }

    private fun renderInventoryChart() {
        val chartContainer = findViewById<LinearLayout>(R.id.chartContainer)
        chartContainer.removeAllViews()
        
        val heights = listOf(40, 70, 55, 90, 60, 85, 45) 
        for ((index, height) in heights.withIndex()) {
            val bar = View(this)
            val params = LinearLayout.LayoutParams(0, (height * 3))
            params.weight = 1f
            params.setMargins(12, 0, 12, 0)
            bar.layoutParams = params
            val color = if (index % 2 == 0) R.color.primary_gold else R.color.accent_indigo
            bar.setBackgroundColor(ContextCompat.getColor(this, color))
            bar.alpha = 0.9f
            chartContainer.addView(bar)
        }
    }
    
    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_home
        fetchLiveMetrics()
        fetchRecentActivity()
    }
}
