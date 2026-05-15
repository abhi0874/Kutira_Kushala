package com.mindmatrix.kutirakushala

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.View
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
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ArtisanActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ArtisanAdapter
    private val artisanList = mutableListOf<Artisan>()
    private val filteredList = mutableListOf<Artisan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        if (auth.currentUser == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_artisan)
        
        setupToolbar()
        setupRecyclerView()
        setupInteractions()
        setupBottomNav()
        fetchArtisans()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onBackPressedDispatcher.onBackPressed() 
        }

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerArtisans)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ArtisanAdapter(filteredList) { artisan ->
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${artisan.phone}")
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupInteractions() {
        val editSearch = findViewById<EditText>(R.id.editSearchArtisan)
        
        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<FloatingActionButton>(R.id.fabAddArtisan).setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            Toast.makeText(this, "Artisan Enrollment System initializing...", Toast.LENGTH_SHORT).show()
        }

        findViewById<ChipGroup>(R.id.chipGroupFilters).setOnCheckedStateChangeListener { group, _ ->
            group.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            fetchArtisans() 
        }
    }

    private fun fetchArtisans() {
        val progressBar = findViewById<CircularProgressIndicator>(R.id.progressBar)
        val txtNoData = findViewById<TextView>(R.id.txtNoData)
        
        progressBar.visibility = View.VISIBLE
        db.collection("artisans").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                artisanList.clear()
                for (doc in result) {
                    val artisan = doc.toObject(Artisan::class.java).copy(id = doc.id)
                    artisanList.add(artisan)
                }
                
                if (artisanList.isEmpty()) {
                    injectSampleData()
                }
                
                filterList("")
                txtNoData.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Access Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun injectSampleData() {
        val samples = listOf(
            Artisan(name = "Rajesh Kumar", specialty = "Master Potter", location = "Varanasi Cluster", phone = "9876543210"),
            Artisan(name = "Sunita Devi", specialty = "Silk Weaver", location = "Kanchipuram Cluster", phone = "9876543211"),
            Artisan(name = "Amit Singh", specialty = "Metal Smith", location = "Jaipur Cluster", phone = "9876543212")
        )
        artisanList.addAll(samples)
    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(artisanList)
        } else {
            val lowerCaseQuery = query.lowercase()
            artisanList.filter { 
                it.name.lowercase().contains(lowerCaseQuery) || 
                it.specialty.lowercase().contains(lowerCaseQuery) ||
                it.location.lowercase().contains(lowerCaseQuery)
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
