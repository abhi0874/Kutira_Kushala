package com.mindmatrix.kutirakushala

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BuyerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: BuyerAdapter
    private val buyerList = mutableListOf<Buyer>()
    private val filteredList = mutableListOf<Buyer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_buyer)
        
        setupToolbar()
        setupRecyclerView()
        setupInteractions()
        setupBottomNav()
        fetchBuyers()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { 
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onBackPressedDispatcher.onBackPressed() 
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerBuyers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BuyerAdapter(filteredList) { buyer ->
            // Placeholder for viewing buyer profile or contact
            Toast.makeText(this, "Partner Profile: ${buyer.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun setupInteractions() {
        val editSearch = findViewById<EditText>(R.id.editSearchBuyer)
        editSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                filterList(v.text.toString())
                true
            } else false
        }

        findViewById<FloatingActionButton>(R.id.fabAddBuyer).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Toast.makeText(this, "Buyer Registration Protocol Offline", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBuyers() {
        val progressBar = findViewById<CircularProgressIndicator>(R.id.progressBar)
        val txtNoData = findViewById<TextView>(R.id.txtNoData)
        
        progressBar.visibility = View.VISIBLE
        db.collection("buyers").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                buyerList.clear()
                for (doc in result) {
                    val buyer = doc.toObject(Buyer::class.java).copy(id = doc.id)
                    buyerList.add(buyer)
                }
                
                if (buyerList.isEmpty()) {
                    injectSampleData()
                }
                
                filterList("")
                txtNoData.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Connection Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun injectSampleData() {
        val samples = listOf(
            Buyer(name = "Global Exports Ltd.", type = "Wholesale Partner", region = "North America", contact = "procurement@global.com"),
            Buyer(name = "Heritage Boutique", type = "Retail Curator", region = "Europe", contact = "buyers@heritage.fr"),
            Buyer(name = "Artisan Alliance", type = "Cooperative", region = "Domestic (India)", contact = "contact@artalliance.in")
        )
        buyerList.addAll(samples)
    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(buyerList)
        } else {
            val lowerCaseQuery = query.lowercase()
            buyerList.filter { 
                it.name.lowercase().contains(lowerCaseQuery) || 
                it.region.lowercase().contains(lowerCaseQuery) 
            }.forEach { filteredList.add(it) }
        }
        adapter.notifyDataSetChanged()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = 0 

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_inventory -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
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
