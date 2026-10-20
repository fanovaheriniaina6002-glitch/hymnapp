package com.example.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ArchivePhotoEntity
import com.example.data.repository.SongRepository
import com.example.ui.adapters.RakitsaryAdapter
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class RakitsaryFragment : Fragment() {

    private lateinit var repository: SongRepository
    private lateinit var rakitsaryAdapter: RakitsaryAdapter

    private var allPhotos: List<ArchivePhotoEntity> = emptyList()
    private var selectedCategory: String = "ALL"
    private var currentDetailPhoto: ArchivePhotoEntity? = null
    private val likedPhotoIds = mutableSetOf<Int>()

    private var chipAll: MaterialCardView? = null
    private var chipFiangonana: MaterialCardView? = null
    private var chipMpitandrina: MaterialCardView? = null
    private var chipFotoana: MaterialCardView? = null

    private var iconChipAll: ImageView? = null
    private var tvChipAll: TextView? = null
    private var tvChipFiangonana: TextView? = null
    private var tvChipMpitandrina: TextView? = null
    private var tvChipFotoana: TextView? = null

    private var tvCount: TextView? = null

    // PhotoDetailScreen (#lightbox-modal) views
    private var modalContainer: View? = null
    private var tvModalCounterPill: TextView? = null
    private var iconModalFavorite: ImageView? = null
    private var ivModalPhoto: ImageView? = null
    private var tvModalCategoryBadge: TextView? = null
    private var tvModalYearBadge: TextView? = null
    private var tvModalTitle: TextView? = null
    private var tvModalLocation: TextView? = null
    private var tvModalDescription: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_rakitsary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())

        val btnBack: View? = view.findViewById(R.id.btn_rakitsary_back)
        val btnCamera: View? = view.findViewById(R.id.btn_rakitsary_camera)
        val rvGrid: RecyclerView? = view.findViewById(R.id.rv_rakitsary_grid)

        tvCount = view.findViewById(R.id.tv_rakitsary_count)

        chipAll = view.findViewById(R.id.chip_rakitsary_all)
        chipFiangonana = view.findViewById(R.id.chip_rakitsary_foibe)
        chipMpitandrina = view.findViewById(R.id.chip_rakitsary_daoud)
        chipFotoana = view.findViewById(R.id.chip_rakitsary_fifohazana)

        iconChipAll = view.findViewById(R.id.icon_chip_rakitsary_all)
        tvChipAll = view.findViewById(R.id.tv_chip_rakitsary_all)
        tvChipFiangonana = view.findViewById(R.id.tv_chip_rakitsary_foibe)
        tvChipMpitandrina = view.findViewById(R.id.tv_chip_rakitsary_daoud)
        tvChipFotoana = view.findViewById(R.id.tv_chip_rakitsary_fifohazana)

        // PhotoDetailScreen (#lightbox-modal) bindings
        modalContainer = view.findViewById(R.id.rakitsary_detail_modal_container)
        val btnModalCloseIcon: View? = view.findViewById(R.id.btn_modal_close_icon)
        val btnModalFavorite: View? = view.findViewById(R.id.btn_modal_favorite)
        val btnModalShare: View? = view.findViewById(R.id.btn_modal_share)
        val btnModalPrev: View? = view.findViewById(R.id.btn_modal_prev_photo)
        val btnModalNext: View? = view.findViewById(R.id.btn_modal_next_photo)

        tvModalCounterPill = view.findViewById(R.id.tv_modal_counter_pill)
        iconModalFavorite = view.findViewById(R.id.icon_modal_favorite)
        ivModalPhoto = view.findViewById(R.id.iv_modal_photo)
        tvModalCategoryBadge = view.findViewById(R.id.tv_modal_category_badge)
        tvModalYearBadge = view.findViewById(R.id.tv_modal_year_badge)
        tvModalTitle = view.findViewById(R.id.tv_modal_title)
        tvModalLocation = view.findViewById(R.id.tv_modal_location)
        tvModalDescription = view.findViewById(R.id.tv_modal_description)

        btnBack?.setOnClickListener {
            (activity as? MainActivity)?.closeRakitsary()
        }

        btnCamera?.setOnClickListener {
            allPhotos.firstOrNull()?.let { openPhotoDetailScreen(it) }
        }

        btnModalCloseIcon?.setOnClickListener {
            closePhotoDetailModal()
        }

        btnModalFavorite?.setOnClickListener {
            val photo = currentDetailPhoto ?: return@setOnClickListener
            if (likedPhotoIds.contains(photo.id)) {
                likedPhotoIds.remove(photo.id)
            } else {
                likedPhotoIds.add(photo.id)
            }
            updateFavoriteButtonVisual(photo.id)
        }

        btnModalShare?.setOnClickListener {
            val photo = currentDetailPhoto ?: return@setOnClickListener
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "${photo.title} — Jesosy Mamonjy M. A. Daoud")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${photo.title} (${photo.year})\n${photo.location}\n\n${photo.description}"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Hizara ny sary tahiry"))
        }

        btnModalPrev?.setOnClickListener {
            val current = currentDetailPhoto ?: return@setOnClickListener
            val index = allPhotos.indexOfFirst { it.id == current.id }
            if (allPhotos.isNotEmpty()) {
                val prevIndex = if (index > 0) index - 1 else allPhotos.lastIndex
                openPhotoDetailScreen(allPhotos[prevIndex])
            }
        }

        btnModalNext?.setOnClickListener {
            val current = currentDetailPhoto ?: return@setOnClickListener
            val index = allPhotos.indexOfFirst { it.id == current.id }
            if (allPhotos.isNotEmpty()) {
                val nextIndex = if (index in 0 until allPhotos.lastIndex) index + 1 else 0
                openPhotoDetailScreen(allPhotos[nextIndex])
            }
        }

        // Category Filter Chips ("Rehetra (41)", "Fiangonana (35)", "Mpitandrina", "Fotoana Manamarika")
        chipAll?.setOnClickListener { applyCategoryFilter("ALL") }
        chipFiangonana?.setOnClickListener { applyCategoryFilter("FIANGONANA") }
        chipMpitandrina?.setOnClickListener { applyCategoryFilter("MPITANDRINA") }
        chipFotoana?.setOnClickListener { applyCategoryFilter("FOTOANA") }

        // 2-Column Photo Gallery Grid: clicking ANY image (regardless of category) opens PhotoDetailScreen
        rakitsaryAdapter = RakitsaryAdapter { clickedPhoto ->
            openPhotoDetailScreen(clickedPhoto)
        }
        rvGrid?.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(false)
            itemAnimator = null // 0% blur / 60 FPS rendering
            adapter = rakitsaryAdapter
        }

        // Observe Offline Room Archive Photos
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allArchivePhotos.collect { photos ->
                    allPhotos = photos
                    renderFilteredGrid()
                }
            }
        }
    }

    private fun applyCategoryFilter(category: String) {
        selectedCategory = category
        updateFilterChipsVisuals(category)
        renderFilteredGrid()
    }

    private fun updateFilterChipsVisuals(activeCategory: String) {
        val ctx = context ?: return
        val activeBg = ContextCompat.getColor(ctx, R.color.primary_container)
        val inactiveBg = ContextCompat.getColor(ctx, R.color.surface_container_lowest)
        val white = ContextCompat.getColor(ctx, R.color.white)
        val inactiveText = 0xFF625E55.toInt()

        data class FilterChip(
            val key: String,
            val card: MaterialCardView?,
            val label: TextView?,
            val icon: ImageView? = null
        )

        val chips = listOf(
            FilterChip("ALL", chipAll, tvChipAll, iconChipAll),
            FilterChip("FIANGONANA", chipFiangonana, tvChipFiangonana),
            FilterChip("MPITANDRINA", chipMpitandrina, tvChipMpitandrina),
            FilterChip("FOTOANA", chipFotoana, tvChipFotoana)
        )

        chips.forEach { chip ->
            val isSelected = chip.key == activeCategory
            chip.card?.setCardBackgroundColor(if (isSelected) activeBg else inactiveBg)
            chip.label?.setTextColor(if (isSelected) white else inactiveText)
            chip.icon?.let {
                ImageViewCompat.setImageTintList(
                    it,
                    ColorStateList.valueOf(if (isSelected) white else inactiveText)
                )
            }
        }
    }

    private fun renderFilteredGrid() {
        val filtered = when (selectedCategory) {
            "ALL" -> allPhotos
            "FIANGONANA" -> allPhotos.filter {
                it.category.equals("FIANGONANA", ignoreCase = true) ||
                    it.category.equals("TANTARA", ignoreCase = true)
            }
            "MPITANDRINA" -> allPhotos.filter {
                it.category.equals("MPITANDRINA", ignoreCase = true)
            }
            "FOTOANA" -> allPhotos.filter {
                it.category.equals("FOTOANA", ignoreCase = true) ||
                    it.category.equals("TANTARA", ignoreCase = true)
            }
            else -> allPhotos
        }
        rakitsaryAdapter.submitList(filtered)
    }

    /**
     * Immediately navigates to PhotoDetailScreen (#lightbox-modal) for ANY clicked image
     * regardless of its category ("FIANGONANA", "FOTOANA", "MPITANDRINA", or "TANTARA").
     */
    fun openPhotoDetailScreen(photo: ArchivePhotoEntity) {
        currentDetailPhoto = photo
        val position = (allPhotos.indexOfFirst { it.id == photo.id } + 1).coerceAtLeast(1)

        tvModalCounterPill?.text = "$position / 41"
        ivModalPhoto?.setImageResource(RakitsaryAdapter.resolveArchiveDrawable(photo.drawableKey))
        tvModalCategoryBadge?.text = photo.category.uppercase()
        tvModalYearBadge?.text = photo.year
        tvModalTitle?.text = photo.title
        tvModalLocation?.text = photo.location
        tvModalDescription?.text = photo.description
        updateFavoriteButtonVisual(photo.id)

        modalContainer?.visibility = View.VISIBLE
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }

    fun closePhotoDetailModal() {
        modalContainer?.visibility = View.GONE
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    private fun updateFavoriteButtonVisual(photoId: Int) {
        val isLiked = likedPhotoIds.contains(photoId)
        iconModalFavorite?.setImageResource(
            if (isLiked) R.drawable.ic_heart else R.drawable.ic_favorite_border
        )
    }

    fun isPhotoDetailModalVisible(): Boolean = modalContainer?.visibility == View.VISIBLE
}
