package com.example.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.HymnCatalog
import com.example.data.local.SongEntity
import com.example.data.repository.SongRepository
import com.example.ui.adapters.HiraTiakoSongAdapter
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HiraTiakoFragment : Fragment() {

    private lateinit var repository: SongRepository
    private lateinit var favoriteAdapter: HiraTiakoSongAdapter

    private var allFavoriteSongs: List<SongEntity> = emptyList()
    private var currentCategoryFilter: String = "REHETRA" // "REHETRA", "LEHIBE", "FANAMPINY"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_hira_tiako, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        HymnCatalog.ensureInitialized(ctx)
        val dao = AppDatabase.getInstance(ctx).songDao()
        repository = SongRepository(dao, ctx.applicationContext)

        val btnBack = view.findViewById<MaterialCardView>(R.id.btn_hira_tiako_back)
        val btnTopFav = view.findViewById<MaterialCardView>(R.id.btn_hira_tiako_top_fav)

        val chipAll = view.findViewById<MaterialCardView>(R.id.chip_fav_all)
        val chipLehibe = view.findViewById<MaterialCardView>(R.id.chip_fav_lehibe)
        val chipFanampiny = view.findViewById<MaterialCardView>(R.id.chip_fav_fanampiny)

        val iconChipAll = view.findViewById<ImageView>(R.id.icon_fav_chip_all)
        val tvChipAll = view.findViewById<TextView>(R.id.tv_fav_chip_all)
        val iconChipLehibe = view.findViewById<ImageView>(R.id.icon_fav_chip_lehibe)
        val tvChipLehibe = view.findViewById<TextView>(R.id.tv_fav_chip_lehibe)
        val iconChipFanampiny = view.findViewById<ImageView>(R.id.icon_fav_chip_fanampiny)
        val tvChipFanampiny = view.findViewById<TextView>(R.id.tv_fav_chip_fanampiny)

        val tvCount = view.findViewById<TextView>(R.id.tv_hira_tiako_count)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty_favorites)
        val rvFavorites = view.findViewById<RecyclerView>(R.id.rv_favorite_songs)

        favoriteAdapter = HiraTiakoSongAdapter(
            onSongClick = { song ->
                HymnCatalog.recordSongConsulted(requireContext(), song.number)
                (activity as? MainActivity)?.openHymnReader(song.number)
            },
            onRemoveFavorite = { song ->
                HymnCatalog.toggleFavorite(requireContext(), song.number)
                Toast.makeText(
                    requireContext(),
                    "Nesorina tao amin'ny Hira Tiako ny Fihirana #${song.numberStr}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        rvFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        btnBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(MainActivity.TAB_FANDRAISANA)
        }

        btnTopFav.setOnClickListener {
            rvFavorites.smoothScrollToPosition(0)
        }

        fun updateFilterChipsUI() {
            val activeBg = ContextCompat.getColor(requireContext(), R.color.chip_active_bg)
            val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            val maroonText = ContextCompat.getColor(requireContext(), R.color.chip_active_bg)

            fun styleChip(
                card: MaterialCardView,
                icon: ImageView,
                tv: TextView,
                isActive: Boolean
            ) {
                card.setCardBackgroundColor(if (isActive) activeBg else transparent)
                icon.imageTintList = ColorStateList.valueOf(if (isActive) white else maroonText)
                tv.setTextColor(if (isActive) white else maroonText)
            }

            styleChip(chipAll, iconChipAll, tvChipAll, currentCategoryFilter == "REHETRA")
            styleChip(chipLehibe, iconChipLehibe, tvChipLehibe, currentCategoryFilter == "LEHIBE")
            styleChip(chipFanampiny, iconChipFanampiny, tvChipFanampiny, currentCategoryFilter == "FANAMPINY")
        }

        fun applyFilterAndRender() {
            val filtered = when (currentCategoryFilter) {
                "LEHIBE" -> allFavoriteSongs.filter { it.category == "Fihirana Taloha" }
                "FANAMPINY" -> allFavoriteSongs.filter { it.category == "Fihirana Vaovao" }
                else -> allFavoriteSongs
            }
            favoriteAdapter.submitList(filtered)
            tvCount.text = "${filtered.size} hira voatahiry"
            tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        }

        chipAll.setOnClickListener {
            currentCategoryFilter = "REHETRA"
            updateFilterChipsUI()
            applyFilterAndRender()
        }
        chipLehibe.setOnClickListener {
            currentCategoryFilter = "LEHIBE"
            updateFilterChipsUI()
            applyFilterAndRender()
        }
        chipFanampiny.setOnClickListener {
            currentCategoryFilter = "FANAMPINY"
            updateFilterChipsUI()
            applyFilterAndRender()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.favoriteSongs.collectLatest { favorites ->
                    allFavoriteSongs = favorites
                    applyFilterAndRender()
                }
            }
        }
    }
}
