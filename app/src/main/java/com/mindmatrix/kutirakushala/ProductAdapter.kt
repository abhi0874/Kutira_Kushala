package com.mindmatrix.kutirakushala

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private val products: List<Product>,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtProductName)
        val category: TextView = view.findViewById(R.id.txtProductCategory)
        val stock: TextView = view.findViewById(R.id.txtProductStock)
        val price: TextView = view.findViewById(R.id.txtProductPrice)
        val icon: ShapeableImageView = view.findViewById(R.id.imgProduct)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.name.text = product.name
        holder.category.text = product.category
        holder.stock.text = product.stock.toString()
        
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        holder.price.text = format.format(product.price)
        
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount() = products.size
}
