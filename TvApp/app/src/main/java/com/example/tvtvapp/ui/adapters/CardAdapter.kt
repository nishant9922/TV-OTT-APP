package com.example.tvtvapp.ui.adapters

import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvtvapp.MainActivity
import com.example.tvtvapp.R
import com.example.tvtvapp.data.Card

class CardAdapter(
    private val cards: List<Card>,
    private val isFirstRail: Boolean,
    private val onClick: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.cardImage)
        val title: TextView = v.findViewById(R.id.cardTitle)
        val handler = Handler(Looper.getMainLooper())
        var longPressTriggered = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_card, parent, false)
        )
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: VH, position: Int) {
        val card = cards[position]

        holder.title.text = card.title
        Glide.with(holder.img).load(card.imageUrl).into(holder.img)

        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true

        holder.itemView.setOnClickListener {
            if (!holder.longPressTriggered) {
                onClick(card)
            }
        }

        holder.itemView.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                holder.handler.removeCallbacksAndMessages(null)
                holder.longPressTriggered = false
            }

            v.animate()
                .scaleX(if (hasFocus) 1.12f else 1f)
                .scaleY(if (hasFocus) 1.12f else 1f)
                .translationZ(if (hasFocus) 16f else 0f)
                .setDuration(150)
                .start()
        }

        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                event.action == KeyEvent.ACTION_DOWN &&
                holder.bindingAdapterPosition == 0 &&
                isFirstRail
            ) {
                (v.context as? MainActivity)?.openDrawerFromRails()
                return@setOnKeyListener true
            }


            if (
                keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
                keyCode != KeyEvent.KEYCODE_ENTER
            ) return@setOnKeyListener false

            val activity = v.context as? MainActivity
                ?: return@setOnKeyListener false

            when (event.action) {

                KeyEvent.ACTION_DOWN -> {
                    holder.longPressTriggered = false
                    v.isPressed = true

                    holder.handler.postDelayed({
                        holder.longPressTriggered = true
                        activity.blockNextCenterKeyUp = true

                        v.isPressed = false
                        v.clearFocus()
                        activity.focusMiniPlayer()
                    }, 3000)

                    false
                }

                KeyEvent.ACTION_UP -> {
                    holder.handler.removeCallbacksAndMessages(null)

                    if (holder.longPressTriggered) {
                        v.isPressed = false
                        v.cancelPendingInputEvents()
                        v.clearFocus()
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.handler.removeCallbacksAndMessages(null)
    }

    override fun getItemCount(): Int = cards.size
}
