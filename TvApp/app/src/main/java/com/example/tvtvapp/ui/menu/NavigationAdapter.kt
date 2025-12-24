package com.example.tvtvapp.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tvtvapp.R
import com.example.tvtvapp.model.MenuItemModel

class NavigationAdapter(
    private val items: List<MenuItemModel>,
    private val onClick: (MenuItemModel) -> Unit
) : RecyclerView.Adapter<NavigationAdapter.MenuViewHolder>() {

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.menuTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.nav_drawer_menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title

        // ✨ Smooth focus animation
        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.08f else 1.0f)
                .scaleY(if (hasFocus) 1.08f else 1.0f)
                .setDuration(120)
                .start()

            holder.title.setTextColor(
                if (hasFocus) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt()
            )
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
