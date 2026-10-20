package com.example.ui.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.local.SongEntity
import com.google.android.material.card.MaterialCardView

/**
 * Rule 4: Lightweight & Memoized Card Adapter for Hikaroka (Search / Browse).
 * Uses only the 6 lightweight pre-indexed properties:
 * id, numberStr, category, title, snippet, isFavorite.
 * Never reads or processes fullLyrics during scroll.
 */
class HikarokaSongAdapter(
    private val onSongClick: (SongEntity) -> Unit,
    private val onFavoriteDoubleTap: ((SongEntity) -> Unit)? = null
) : ListAdapter<SongEntity, HikarokaSongAdapter.HikarokaSongViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    var currentQuery: String = ""

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HikarokaSongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hikaroka_song, parent, false)
        return HikarokaSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: HikarokaSongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HikarokaSongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_hikaroka_song)
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_song_number)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_song_title)
        private val tvPreview: TextView = itemView.findViewById(R.id.tv_song_preview)
        private val btnFav: ImageButton = itemView.findViewById(R.id.btn_favorite_toggle)

        private val activeColor = ContextCompat.getColor(itemView.context, R.color.primary_container)
        private val inactiveColor = ContextCompat.getColor(itemView.context, R.color.outline)

        private var boundSong: SongEntity? = null

        @SuppressLint("ClickableViewAccessibility")
        private val gestureDetector = GestureDetector(
            itemView.context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    boundSong?.let(onSongClick)
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    boundSong?.let { song ->
                        onFavoriteDoubleTap?.invoke(song) ?: onSongClick(song)
                    }
                    return true
                }
            }
        )

        init {
            if (onFavoriteDoubleTap != null) {
                card.setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    false
                }
                card.setOnClickListener {
                    // Handled by GestureDetector for single vs double tap
                }
                btnFav.setOnClickListener {
                    boundSong?.let { song -> onFavoriteDoubleTap.invoke(song) }
                }
            } else {
                card.setOnClickListener {
                    boundSong?.let(onSongClick)
                }
            }
        }

        fun bind(song: SongEntity) {
            boundSong = song
            tvNumber.text = song.numberStr
            tvTitle.text = com.example.ui.util.HighlightHelper.highlightText(song.title, currentQuery)
            tvPreview.text = com.example.ui.util.HighlightHelper.highlightText("“${song.snippet}”", currentQuery)
            if (song.isFavorite) {
                btnFav.setImageResource(R.drawable.ic_heart)
                btnFav.imageTintList = ColorStateList.valueOf(activeColor)
            } else {
                btnFav.setImageResource(R.drawable.ic_favorite_border)
                btnFav.imageTintList = ColorStateList.valueOf(inactiveColor)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SongEntity>() {
            override fun areItemsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean =
                oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.snippet == newItem.snippet &&
                    oldItem.category == newItem.category &&
                    oldItem.isFavorite == newItem.isFavorite
        }
    }
}
