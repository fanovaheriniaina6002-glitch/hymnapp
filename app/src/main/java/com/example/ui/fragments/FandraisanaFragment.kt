package com.example.ui.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.HymnCatalog
import com.example.data.local.OnboardingPreferencesManager
import com.example.data.local.SongEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.SongRepository
import com.example.ui.adapters.CategoryAdapter
import com.example.ui.adapters.SongAdapter
import com.example.ui.theme.ThemeManager
import com.google.android.material.card.MaterialCardView
import android.graphics.drawable.ColorDrawable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FandraisanaFragment : Fragment() {

    private lateinit var repository: SongRepository
    private lateinit var onboardingPrefs: OnboardingPreferencesManager
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var songAdapter: SongAdapter

    private var currentFeaturedSong: SongEntity? = null
    private var currentProfile: UserProfileEntity? = null
    private var historySongsCache: List<SongEntity> = emptyList()
    private var searchDebounceJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fandraisana, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        HymnCatalog.ensureInitialized(ctx)
        val dao = AppDatabase.getInstance(ctx).songDao()
        repository = SongRepository(dao, ctx.applicationContext)
        onboardingPrefs = OnboardingPreferencesManager(ctx)

        val tvWelcomeTitle = view.findViewById<TextView>(R.id.tv_welcome_title)
        val ivTopProfileAvatar = view.findViewById<ImageView>(R.id.iv_top_profile_avatar)
        val btnTopProfile = view.findViewById<MaterialCardView>(R.id.btn_top_profile)

        val tvHeroCollection = view.findViewById<TextView>(R.id.tv_hero_collection)
        val tvHeroTitle = view.findViewById<TextView>(R.id.tv_hero_song_title)
        val tvHeroExcerpt = view.findViewById<TextView>(R.id.tv_hero_song_excerpt)
        val btnHeroRead = view.findViewById<MaterialCardView>(R.id.btn_hero_read)
        val cardHeroSong = view.findViewById<MaterialCardView>(R.id.card_hero_song)

        val etSearchInput = view.findViewById<EditText>(R.id.et_search_input)
        val btnNumpadTrigger = view.findViewById<MaterialCardView>(R.id.btn_numpad)

        val cardBentoTrakta = view.findViewById<MaterialCardView>(R.id.card_bento_trakta)
        val cardBentoFotoana = view.findViewById<MaterialCardView>(R.id.card_bento_fotoana)
        val cardBentoRakitsary = view.findViewById<MaterialCardView>(R.id.card_bento_rakitsary)

        val rvCategories = view.findViewById<RecyclerView>(R.id.rv_categories)
        val rvSongs = view.findViewById<RecyclerView>(R.id.rv_songs)
        val cardHistoryEmptyState = view.findViewById<MaterialCardView>(R.id.card_history_empty_state)
        val btnSeeAllSongs = view.findViewById<MaterialCardView>(R.id.btn_see_all_songs)

        val btnTopThemeToggle = view.findViewById<MaterialCardView>(R.id.btn_top_theme_toggle)
        val ivTopThemeIcon = view.findViewById<ImageView>(R.id.iv_top_theme_icon)
        val floatingTopBar = view.findViewById<MaterialCardView>(R.id.floating_top_bar)
        val fandraisanaScrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.fandraisana_scroll_view)
        val tvAppTitle = view.findViewById<TextView>(R.id.tv_app_title)
        val tvAppSubtitle = view.findViewById<TextView>(R.id.tv_app_subtitle)

        val cardSearchBar = view.findViewById<MaterialCardView>(R.id.card_search_bar)
        val tvBentoTraktaTitle = view.findViewById<TextView>(R.id.tv_bento_trakta_title)
        val tvBentoTraktaSub = view.findViewById<TextView>(R.id.tv_bento_trakta_sub)
        val tvBentoFotoanaTitle = view.findViewById<TextView>(R.id.tv_bento_fotoana_title)
        val tvBentoFotoanaSub = view.findViewById<TextView>(R.id.tv_bento_fotoana_sub)
        val tvBentoRakitsaryTitle = view.findViewById<TextView>(R.id.tv_bento_rakitsary_title)
        val tvBentoRakitsarySub = view.findViewById<TextView>(R.id.tv_bento_rakitsary_sub)

        btnTopThemeToggle?.setOnClickListener {
            (activity as? MainActivity)?.toggleThemeMode()
        }

        cardHistoryEmptyState?.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(MainActivity.TAB_HIKAROKA)
        }

        // Setup Categories Grid (2 columns)
        categoryAdapter = CategoryAdapter { category ->
            (activity as? MainActivity)?.openHikarokaWithSearch(
                query = "",
                openNumpad = false
            )
            Toast.makeText(
                requireContext(),
                "${category.name} (${category.songCount} hira)",
                Toast.LENGTH_SHORT
            ).show()
        }
        rvCategories.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = categoryAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
        }

        // Rule 4 & 5: Setup "Hira nampiasaina farany" (FIFO 10 consulted songs)
        songAdapter = SongAdapter(
            onSongClick = { song ->
                HymnCatalog.recordSongConsulted(requireContext(), song.number)
                (activity as? MainActivity)?.openHymnReader(song.number)
            },
            onFavoriteToggle = { song ->
                val added = HymnCatalog.toggleFavorite(requireContext(), song.number)
                val msg = if (added) {
                    "Voatahiry ao amin'ny Hira Tiako ny Fihirana #${song.numberStr}"
                } else {
                    "Nesorina tao amin'ny Hira Tiako ny Fihirana #${song.numberStr}"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
        rvSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
        }

        // Top Profile Avatar -> Open Kaonty tab
        btnTopProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(MainActivity.TAB_KAONTY)
        }

        // Hero Card Click & Read Button -> Record in History & Open HymnReader
        val openHeroSong = View.OnClickListener {
            val hero = currentFeaturedSong ?: HymnCatalog.getSongByNumber(requireContext(), 112)
                ?: HymnCatalog.getSongByNumber(requireContext(), 1)
            if (hero != null) {
                HymnCatalog.recordSongConsulted(requireContext(), hero.number)
                (activity as? MainActivity)?.openHymnReader(hero.number)
            }
        }
        btnHeroRead.setOnClickListener(openHeroSong)
        cardHeroSong.setOnClickListener(openHeroSong)

        // Search Input Dual-Speed Filtering on Dashboard or Jump to Hikaroka on Enter
        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                searchDebounceJob?.cancel()
                if (query.isEmpty()) {
                    val isEmpty = historySongsCache.isEmpty()
                    songAdapter.submitList(historySongsCache)
                    cardHistoryEmptyState?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    rvSongs.visibility = if (isEmpty) View.GONE else View.VISIBLE
                } else if (query.all { it.isDigit() }) {
                    // Rule 3: 0ms instant number search
                    val matches = HymnCatalog.searchSongs(
                        context = requireContext(),
                        rawQuery = query,
                        categoryFilter = "REHETRA"
                    ).take(10)
                    songAdapter.submitList(matches)
                    cardHistoryEmptyState?.visibility = View.GONE
                    rvSongs.visibility = View.VISIBLE
                } else {
                    // Rule 3: 120ms debounce for text search
                    searchDebounceJob = viewLifecycleOwner.lifecycleScope.launch {
                        delay(120L)
                        val matches = HymnCatalog.searchSongs(
                            context = requireContext(),
                            rawQuery = query,
                            categoryFilter = "REHETRA"
                        ).take(10)
                        songAdapter.submitList(matches)
                        cardHistoryEmptyState?.visibility = View.GONE
                        rvSongs.visibility = View.VISIBLE
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        etSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val q = etSearchInput.text?.toString()?.trim().orEmpty()
                (activity as? MainActivity)?.openHikarokaWithSearch(
                    query = q,
                    openNumpad = false
                )
                true
            } else {
                false
            }
        }

        // Numpad Trigger Button -> Open Hikaroka with Numpad Sheet
        btnNumpadTrigger.setOnClickListener {
            (activity as? MainActivity)?.openHikarokaWithSearch(
                query = "",
                openNumpad = true
            )
        }

        // Bento Hub Cards Navigation
        cardBentoTrakta.setOnClickListener {
            (activity as? MainActivity)?.openTrakta()
        }
        cardBentoFotoana.setOnClickListener {
            (activity as? MainActivity)?.openFandaharampotoana()
        }
        cardBentoRakitsary.setOnClickListener {
            (activity as? MainActivity)?.openRakitsary()
        }

        // "Hijery rehetra" Button -> Navigate to Hikaroka tab
        btnSeeAllSongs.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(MainActivity.TAB_HIKAROKA)
        }

        applyOnboardingWelcomeFromPrefs(tvWelcomeTitle, ivTopProfileAvatar)

        // Observe Flows
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    onboardingPrefs.onboardingDataFlow.collectLatest { userData ->
                        val savedName = userData.name.ifBlank {
                            currentProfile?.displayName ?: "Frère Aina"
                        }
                        val savedGender = userData.gender.ifBlank {
                            currentProfile?.gender ?: "Lehilahy"
                        }
                        val savedPhotoUri = userData.imageUri ?: currentProfile?.photoUri.orEmpty()

                        val cleanName = savedName
                            .removePrefix("Rahalahy ")
                            .removePrefix("Ranabavy ")
                            .removePrefix("Anabavy ")
                            .removePrefix("Frère ")
                            .removePrefix("Sœur ")
                            .trim()

                        val prefix = if (savedGender.equals("Vehivavy", ignoreCase = true) ||
                            savedGender.equals("female", ignoreCase = true)
                        ) {
                            "Anabavy"
                        } else {
                            "Rahalahy"
                        }

                        tvWelcomeTitle.text = if (cleanName.isNotEmpty()) {
                            "Salama, $prefix $cleanName"
                        } else {
                            "Salama, Tongasoa"
                        }

                        bindTopAvatar(ivTopProfileAvatar, savedPhotoUri)
                    }
                }

                launch {
                    repository.userProfile.collectLatest { profile ->
                        currentProfile = profile
                    }
                }

                launch {
                    repository.featuredSong.collectLatest { hero ->
                        if (hero != null) {
                            currentFeaturedSong = hero
                            tvHeroCollection.text = hero.category
                            tvHeroTitle.text = "${hero.numberStr}. ${hero.title}"
                            tvHeroExcerpt.text = "“${hero.snippet}”"
                        }
                    }
                }

                launch {
                    repository.allCategories.collectLatest { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }

                // Rule 5: Observe FIFO-10 consultation history without duplicates
                launch {
                    repository.historySongs.collectLatest { historySongs ->
                        historySongsCache = historySongs
                        val currentQuery = etSearchInput.text?.toString()?.trim().orEmpty()
                        if (currentQuery.isEmpty()) {
                            val isEmpty = historySongs.isEmpty()
                            songAdapter.submitList(historySongs)
                            cardHistoryEmptyState?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                            rvSongs.visibility = if (isEmpty) View.GONE else View.VISIBLE
                        }
                    }
                }

                // 60 FPS Dark Mode Observer (Instant theme switch without reload)
                launch {
                    ThemeManager.isDarkMode.collectLatest { isDark ->
                        ivTopThemeIcon?.setImageResource(
                            if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                        )
                        ivTopThemeIcon?.imageTintList = ColorStateList.valueOf(
                            if (isDark) 0xFFD4AF37.toInt() else ContextCompat.getColor(ctx, R.color.primary_container)
                        )

                        // 1. Top Bar
                        floatingTopBar?.setCardBackgroundColor(if (isDark) 0xFF1C1C1E.toInt() else 0xFFFFFFFF.toInt())
                        floatingTopBar?.strokeColor = if (isDark) 0xFF38383A.toInt() else 0xFFEAE0E2.toInt()
                        tvAppTitle?.setTextColor(if (isDark) 0xFFF5E6E8.toInt() else ContextCompat.getColor(ctx, R.color.primary_container))
                        tvAppSubtitle?.setTextColor(if (isDark) 0xFFD4AF37.toInt() else ContextCompat.getColor(ctx, R.color.on_surface_variant))
                        btnTopProfile?.setCardBackgroundColor(if (isDark) 0xFF2C2C2E.toInt() else ContextCompat.getColor(ctx, R.color.pill_btn_bg))
                        btnTopThemeToggle?.setCardBackgroundColor(if (isDark) 0xFF2C2C2E.toInt() else ContextCompat.getColor(ctx, R.color.pill_btn_bg))

                        // 2. Screen background (Solid Noir pur #000000 in dark mode)
                        fandraisanaScrollView?.background = if (isDark) {
                            ColorDrawable(0xFF000000.toInt())
                        } else {
                            ContextCompat.getDrawable(ctx, R.drawable.bg_screen_gradient)
                        }

                        // 3. Search Bar
                        cardSearchBar?.setCardBackgroundColor(if (isDark) 0xFF1C1C1E.toInt() else 0xFFFFFFFF.toInt())
                        cardSearchBar?.strokeColor = if (isDark) 0xFF38383A.toInt() else 0xFFEAE0E2.toInt()
                        etSearchInput.setTextColor(if (isDark) 0xFFF5E6E8.toInt() else ContextCompat.getColor(ctx, R.color.on_surface))
                        etSearchInput.setHintTextColor(if (isDark) 0xFF8E8E93.toInt() else ContextCompat.getColor(ctx, R.color.on_surface_variant_hint))

                        // 4. Bento Hub Cards
                        val bentoCardBg = if (isDark) 0xFF1C1C1E.toInt() else 0xFFFFFFFF.toInt()
                        val bentoStroke = if (isDark) 0xFF38383A.toInt() else 0xFFEAE0E2.toInt()
                        val bentoTitleColor = if (isDark) 0xFFF5E6E8.toInt() else ContextCompat.getColor(ctx, R.color.bento_card_title)
                        val bentoSubColor = if (isDark) 0xFFD4AF37.toInt() else ContextCompat.getColor(ctx, R.color.on_surface_variant)

                        cardBentoTrakta?.setCardBackgroundColor(bentoCardBg)
                        cardBentoTrakta?.strokeColor = bentoStroke
                        tvBentoTraktaTitle?.setTextColor(bentoTitleColor)
                        tvBentoTraktaSub?.setTextColor(bentoSubColor)

                        cardBentoFotoana?.setCardBackgroundColor(bentoCardBg)
                        cardBentoFotoana?.strokeColor = bentoStroke
                        tvBentoFotoanaTitle?.setTextColor(bentoTitleColor)
                        tvBentoFotoanaSub?.setTextColor(bentoSubColor)

                        cardBentoRakitsary?.setCardBackgroundColor(bentoCardBg)
                        cardBentoRakitsary?.strokeColor = bentoStroke
                        tvBentoRakitsaryTitle?.setTextColor(bentoTitleColor)
                        tvBentoRakitsarySub?.setTextColor(bentoSubColor)

                        // 5. Empty State Card
                        cardHistoryEmptyState?.setCardBackgroundColor(if (isDark) 0xFF1C1C1E.toInt() else 0xFFFDF9FA.toInt())
                        cardHistoryEmptyState?.strokeColor = if (isDark) 0xFF38383A.toInt() else 0xFFEAE0E2.toInt()

                        songAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun applyOnboardingWelcomeFromPrefs(
        tvWelcomeTitle: TextView,
        ivTopProfileAvatar: ImageView
    ) {
        val prefs = requireContext().getSharedPreferences(
            "jesosy_mamonjy_onboarding_prefs",
            Context.MODE_PRIVATE
        )
        val savedName = prefs.getString("user_name", "")?.trim().orEmpty()
        val savedGender = prefs.getString("user_gender", "Lehilahy").orEmpty()
        val savedPhotoUri = prefs.getString("profile_image_uri", "").orEmpty()

        if (savedName.isNotEmpty()) {
            val cleanName = savedName
                .removePrefix("Rahalahy ")
                .removePrefix("Ranabavy ")
                .removePrefix("Anabavy ")
                .removePrefix("Frère ")
                .removePrefix("Sœur ")
                .trim()
            val prefix = if (savedGender.equals("Vehivavy", ignoreCase = true) ||
                savedGender.equals("female", ignoreCase = true)
            ) {
                "Anabavy"
            } else {
                "Rahalahy"
            }
            tvWelcomeTitle.text = "Salama, $prefix $cleanName"
        }

        bindTopAvatar(ivTopProfileAvatar, savedPhotoUri)
    }

    private fun bindTopAvatar(imageView: ImageView, photoUriString: String) {
        if (photoUriString.isNotBlank()) {
            runCatching {
                imageView.setImageURI(Uri.parse(photoUriString))
                imageView.imageTintList = null
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val pad = 0
                imageView.setPadding(pad, pad, pad, pad)
            }.onFailure {
                resetDefaultTopAvatar(imageView)
            }
        } else {
            resetDefaultTopAvatar(imageView)
        }
    }

    private fun resetDefaultTopAvatar(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_person)
        imageView.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.primary_container)
        )
        val pad = (6 * resources.displayMetrics.density).toInt()
        imageView.setPadding(pad, pad, pad, pad)
    }
}
