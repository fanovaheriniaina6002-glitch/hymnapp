package com.example.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.local.ArchivePhotoEntity

class RakitsaryAdapter(
    private val onPhotoClick: (ArchivePhotoEntity) -> Unit
) : ListAdapter<ArchivePhotoEntity, RakitsaryAdapter.PhotoViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rakitsary_photo, parent, false)
        return PhotoViewHolder(view, onPhotoClick)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PhotoViewHolder(
        itemView: View,
        private val onPhotoClick: (ArchivePhotoEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardRoot: View = itemView.findViewById(R.id.card_rakitsary_item)
        private val clickContainer: View? = itemView.findViewById(R.id.photo_item_click_container)
        private val ivThumb: ImageView = itemView.findViewById(R.id.iv_photo_thumb)
        private val tvCategoryTag: TextView = itemView.findViewById(R.id.tv_photo_category_tag)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_photo_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_photo_subtitle)

        private var currentPhoto: ArchivePhotoEntity? = null

        init {
            val triggerNavigation = View.OnClickListener {
                currentPhoto?.let(onPhotoClick)
            }
            itemView.setOnClickListener(triggerNavigation)
            cardRoot.setOnClickListener(triggerNavigation)
            clickContainer?.setOnClickListener(triggerNavigation)
            ivThumb.setOnClickListener(triggerNavigation)
        }

        fun bind(item: ArchivePhotoEntity) {
            currentPhoto = item
            tvCategoryTag.text = item.category
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            ivThumb.setImageResource(resolveArchiveDrawable(item.drawableKey))

            // Match category pill styling from HTML (Grenat for FIANGONANA/MPITANDRINA, dark for TANTARA/FOTOANA)
            val badgeRes = when (item.category.uppercase()) {
                "FIANGONANA", "MPITANDRINA" -> R.drawable.bg_badge_primary_pill
                else -> R.drawable.bg_badge_dark_pill
            }
            tvCategoryTag.setBackgroundResource(badgeRes)
        }
    }

    companion object {
        fun resolveArchiveDrawable(key: String): Int {
            return when (key) {
                "archive_1" -> R.drawable.ic_archive_1_gathering
                "archive_2" -> R.drawable.ic_archive_2_pastor
                "archive_3" -> R.drawable.ic_archive_3_globe
                "archive_4", "archive_5" -> R.drawable.ic_archive_4_altar
                "archive_6" -> R.drawable.ic_archive_6_worship
                else -> R.drawable.ic_archive_1_gathering
            }
        }

        private val DiffCallback = object : DiffUtil.ItemCallback<ArchivePhotoEntity>() {
            override fun areItemsTheSame(
                oldItem: ArchivePhotoEntity,
                newItem: ArchivePhotoEntity
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ArchivePhotoEntity,
                newItem: ArchivePhotoEntity
            ): Boolean = oldItem == newItem
        }
    }
}
