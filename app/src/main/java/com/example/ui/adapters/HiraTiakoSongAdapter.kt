package com.example.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.local.SongEntity
import com.google.android.material.card.MaterialCardView

/**
 * Rule 4 & 5: Lightweight & Memoized Card Adapter for Hira Tiako (Favorites).
 * Uses only pre-indexed properties (id, numberStr, category, title, snippet, isFavorite).
 */
class HiraTiakoSongAdapter(
    private val onSongClick: (SongEntity) -> Unit,
    private val onRemoveFavorite: (SongEntity) -> Unit
) : ListAdapter<SongEntity, HiraTiakoSongAdapter.HiraTiakoViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HiraTiakoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hira_tiako_song, parent, false)
        return HiraTiakoViewHolder(view)
    }

    override fun onBindViewHolder(holder: HiraTiakoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HiraTiakoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_hira_tiako_song)
        private val tvNumber: TextView = itemView.findViewById(R.id.tv_song_number)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_song_title)
        private val tvPreview: TextView = itemView.findViewById(R.id.tv_song_preview)
        private val btnRemoveFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite_toggle)

        private var boundSong: SongEntity? = null

        init {
            card.setOnClickListener {
                boundSong?.let(onSongClick)
            }
            btnRemoveFavorite.setOnClickListener {
                boundSong?.let(onRemoveFavorite)
            }
        }

        fun bind(song: SongEntity) {
            boundSong = song
            tvNumber.text = song.numberStr
            tvTitle.text = song.title
            tvPreview.text = "“${song.snippet}”"
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
