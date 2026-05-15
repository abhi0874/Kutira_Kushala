package com.mindmatrix.kutirakushala

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class ArtisanAdapter(private val artisans: List<Artisan>, private val onCallClick: (Artisan) -> Unit) :
    RecyclerView.Adapter<ArtisanAdapter.ArtisanViewHolder>() {

    class ArtisanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtArtisanName)
        val specialty: TextView = view.findViewById(R.id.txtArtisanSpecialty)
        val location: TextView = view.findViewById(R.id.txtArtisanLocation)
        val avatar: ShapeableImageView = view.findViewById(R.id.imgArtisanAvatar)
        val btnCall: MaterialButton = view.findViewById(R.id.btnCallArtisan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtisanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artisan, parent, false)
        return ArtisanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtisanViewHolder, position: Int) {
        val artisan = artisans[position]
        holder.name.text = artisan.name
        holder.specialty.text = artisan.specialty
        holder.location.text = artisan.location
        
        holder.btnCall.setOnClickListener { onCallClick(artisan) }
        
        // Dynamic avatar tint or icon based on specialty could be added here
    }

    override fun getItemCount() = artisans.size
}
