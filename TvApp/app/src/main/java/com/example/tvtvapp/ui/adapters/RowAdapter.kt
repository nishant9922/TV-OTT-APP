package com.example.tvtvapp.ui.adapters


import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvtvapp.MainActivity
import com.example.tvtvapp.R
import com.example.tvtvapp.data.Card
import com.example.tvtvapp.data.Row

class RowAdapter(
    private val rows: List<Row>,
    private val onCardClick: (Card) -> Unit
) : RecyclerView.Adapter<RowAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.rowTitle)
        val horizontal: RecyclerView = v.findViewById(R.id.horizontalRecycler)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return VH(v)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: VH, rowPosition: Int) {
        val row = rows[rowPosition]
        holder.title.text = row.title

        val context = holder.itemView.context


        holder.horizontal.apply {

            layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = CardAdapter(row.cards, rowPosition == 0,onCardClick)

            isFocusable = false
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

            setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

                val lm = layoutManager as LinearLayoutManager
                val first = lm.findFirstCompletelyVisibleItemPosition()
                val last = lm.findLastCompletelyVisibleItemPosition()
                val lastIndex = adapter!!.itemCount - 1

                when (keyCode) {

                    // ⬅ allow drawer ONLY from first card
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        return@setOnKeyListener first != 0
                        // false → Activity opens drawer
                        // true  → block drawer, allow scroll
                    }

                    // ➡ allow mini player ONLY from last card
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        return@setOnKeyListener last != lastIndex
                        // false → Activity handles
                        // true  → keep scrolling
                    }

                    // ⬆ go to Explore
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        rootView.findViewById<View>(R.id.btnExplore)?.requestFocus()
                        return@setOnKeyListener true
                    }
                }

                false
            }
        }

    }

    override fun getItemCount() = rows.size
}
