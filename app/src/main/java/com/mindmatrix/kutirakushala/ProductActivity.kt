package com.mindmatrix.kutirakushala

import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()
    private val filteredList = mutableListOf<Product>()

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
        setContentView(R.layout.activity_product)
        
        setupToolbar()
        setupRecyclerView()
        setupInteractions()
        setupBottomNav()
        fetchProducts()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { 
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
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
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter(filteredList) { product ->
            showDeleteConfirmation(product)
        }
        recyclerView.adapter = adapter
    }

    private fun showDeleteConfirmation(product: Product) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove Product")
            .setMessage("Are you sure you want to remove ${product.name} from the enterprise catalog? This action cannot be undone.")
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Remove") { _, _ ->
                deleteProduct(product)
            }
            .show()
    }

    private fun deleteProduct(product: Product) {
        if (product.id.isEmpty()) {
            // Probably sample data
            productList.remove(product)
            filterList("")
            return
        }

        db.collection("inventory").document(product.id).delete()
            .addOnSuccessListener {
                productList.remove(product)
                filterList("")
                Snackbar.make(findViewById(android.R.id.content), "Product removed from ledger", Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.bottomNavigation)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Protocol Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupInteractions() {
        val editSearch = findViewById<EditText>(R.id.editSearchProduct)
        editSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                filterList(v.text.toString())
                true
            } else false
        }

        findViewById<FloatingActionButton>(R.id.fabAddProduct).setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun fetchProducts() {
        val progressBar = findViewById<CircularProgressIndicator>(R.id.progressBar)
        val txtNoData = findViewById<TextView>(R.id.txtNoData)
        
        progressBar.visibility = View.VISIBLE
        db.collection("inventory").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                productList.clear()
                for (doc in result) {
                    val product = Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = "Enterprise Grade",
                        stock = doc.getLong("quantity")?.toInt() ?: 0,
                        price = doc.getDouble("price_per_unit") ?: 0.0,
                        description = doc.getString("description") ?: ""
                    )
                    productList.add(product)
                }
                
                if (productList.isEmpty()) {
                    injectSampleData()
                }
                
                filterList("")
                txtNoData.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Fetch Protocol Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun injectSampleData() {
        val samples = listOf(
            Product(name = "Premium Silk Saree", category = "Textiles", stock = 142, price = 45.0),
            Product(name = "Terracotta Vase", category = "Pottery", stock = 56, price = 12.5),
            Product(name = "Hand-painted Scarf", category = "Accessories", stock = 89, price = 22.0)
        )
        productList.addAll(samples)
    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(productList)
        } else {
            val lowerCaseQuery = query.lowercase()
            productList.filter { 
                it.name.lowercase().contains(lowerCaseQuery) || 
                it.category.lowercase().contains(lowerCaseQuery) 
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
