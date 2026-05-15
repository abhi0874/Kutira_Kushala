package com.mindmatrix.kutirakushala

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class BuyerAdapter(private val buyers: List<Buyer>, private val onActionClick: (Buyer) -> Unit) :
    RecyclerView.Adapter<BuyerAdapter.BuyerViewHolder>() {

    class BuyerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtBuyerName)
        val type: TextView = view.findViewById(R.id.txtBuyerType)
        val region: TextView = view.findViewById(R.id.txtBuyerRegion)
        val icon: ShapeableImageView = view.findViewById(R.id.imgBuyer)
        val btnAction: MaterialButton = view.findViewById(R.id.btnBuyerAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_buyer, parent, false)
        return BuyerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyerViewHolder, position: Int) {
        val buyer = buyers[position]
        holder.name.text = buyer.name
        holder.type.text = buyer.type
        holder.region.text = buyer.region
        
        holder.btnAction.setOnClickListener { onActionClick(buyer) }
    }

    override fun getItemCount() = buyers.size
}
