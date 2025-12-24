package com.example.tvtvapp.ui.adapters

import android.content.Intent
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvtvapp.DetailActivity
import com.example.tvtvapp.R
import com.example.tvtvapp.data.Banner

class BannerAdapter(private val items: List<Banner>) :
    RecyclerView.Adapter<BannerAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.bannerImage)
        val title: TextView = v.findViewById(R.id.bannerTitle)
        val tagline: TextView = v.findViewById(R.id.bannerTagline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = items[position]

        holder.title.text = b.title
        holder.tagline.text = b.tagline
        Glide.with(holder.image).load(b.imageUrl).into(holder.image)

        // 🔥 FIXED: send correct key "VIDEO_ID"
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("VIDEO_ID", b.imdbId)
            context.startActivity(intent)
        }

        holder.itemView.isFocusable = true
        holder.itemView.setOnFocusChangeListener { v, focused ->
            v.animate()
                .scaleX(if (focused) 1.04f else 1f)
                .scaleY(if (focused) 1.04f else 1f)
                .setDuration(180)
                .start()
        }

        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                val exploreButton = v.rootView.findViewById<View>(R.id.btnExplore)
                exploreButton?.requestFocus()
                return@setOnKeyListener true
            }
            false
        }
    }

    override fun getItemCount() = items.size
}
