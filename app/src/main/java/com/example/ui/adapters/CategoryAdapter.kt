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
import com.example.data.local.CategoryEntity

class CategoryAdapter(
    private val onCategoryClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_bento, parent, false)
        return CategoryViewHolder(view, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        itemView: View,
        private val onClick: (CategoryEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_category_title)
        private val tvCount: TextView = itemView.findViewById(R.id.tv_category_count)
        private val imgIcon: ImageView = itemView.findViewById(R.id.img_category_icon)

        fun bind(category: CategoryEntity) {
            tvTitle.text = category.title
            tvCount.text = "${category.songCount} Hira • ${category.subtitle}"
            val iconRes = if (category.id.contains("fampianarana")) {
                R.drawable.ic_book
            } else {
                R.drawable.ic_music_note
            }
            imgIcon.setImageResource(iconRes)
            itemView.setOnClickListener { onClick(category) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CategoryEntity>() {
        override fun areItemsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoryEntity, newItem: CategoryEntity): Boolean =
            oldItem == newItem
    }
}
