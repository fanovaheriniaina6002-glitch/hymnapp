package com.example.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.HymnCatalog
import com.example.data.local.SongEntity
import com.example.data.repository.SongRepository
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import com.example.ui.theme.ThemeManager
import com.example.ui.util.HighlightHelper
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Full-Screen Hymn Reader (60 FPS Solid Bento).
 * Loads fullLyrics on demand from HymnCatalog in 0ms and dynamically renders
 * all stanzas with 5-second intelligent search highlight.
 */
class HymnReaderFragment : Fragment() {

    private lateinit var repository: SongRepository
    private var currentSongNumber: Int = 4
    private var currentSong: SongEntity? = null
    private var currentFontSizeSp: Float = 17f

    private var currentSearchQuery: String? = null
    private var isHighlightVisible: Boolean = false
    private var highlightJob: Job? = null

    private var tvHeaderTitle: TextView? = null

    private var tvVerse1: TextView? = null
    private var sectionChorus: View? = null
    private var tvChorus: TextView? = null
    private var sectionVerse2: View? = null
    private var tvVerse2: TextView? = null
    private var sectionVerse3: View? = null
    private var tvVerse3: TextView? = null
    private var extraVersesContainer: LinearLayout? = null
    private var scrollView: NestedScrollView? = null

    private var iconFav: ImageView? = null
    private var tvPrevHymn: TextView? = null
    private var tvNextHymn: TextView? = null

    private val dynamicVerseTextViews = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_hymn_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        HymnCatalog.ensureInitialized(ctx)
        val dao = AppDatabase.getInstance(ctx).songDao()
        repository = SongRepository(dao, ctx.applicationContext)

        val btnBack = view.findViewById<MaterialCardView>(R.id.btn_reader_back)
        val btnThemeToggle = view.findViewById<MaterialCardView>(R.id.btn_reader_theme_toggle)
        tvHeaderTitle = view.findViewById(R.id.tv_reader_header_title)

        tvVerse1 = view.findViewById(R.id.tv_verse_1)
        sectionChorus = view.findViewById(R.id.section_chorus)
        tvChorus = view.findViewById(R.id.tv_chorus)
        sectionVerse2 = view.findViewById(R.id.section_verse_2)
        tvVerse2 = view.findViewById(R.id.tv_verse_2)
        sectionVerse3 = view.findViewById(R.id.section_verse_3)
        tvVerse3 = view.findViewById(R.id.tv_verse_3)
        extraVersesContainer = view.findViewById(R.id.extra_verses_container)
        scrollView = view.findViewById(R.id.reader_scroll_view)

        val btnShare = view.findViewById<MaterialCardView>(R.id.btn_reader_share)
        val btnCopy = view.findViewById<MaterialCardView>(R.id.btn_reader_copy)
        val btnFav = view.findViewById<MaterialCardView>(R.id.btn_reader_fav)
        iconFav = view.findViewById(R.id.icon_reader_fav)

        val btnPrevHymn = view.findViewById<View>(R.id.btn_prev_hymn)
        tvPrevHymn = view.findViewById(R.id.tv_prev_hymn)
        val btnNextHymn = view.findViewById<View>(R.id.btn_next_hymn)
        tvNextHymn = view.findViewById(R.id.tv_next_hymn)

        val btnFontDecrease = view.findViewById<View>(R.id.btn_font_decrease)
        val btnFontIncrease = view.findViewById<View>(R.id.btn_font_increase)

        btnBack.setOnClickListener {
            (activity as? MainActivity)?.closeHymnReader()
        }

        val iconThemeToggle = view.findViewById<ImageView>(R.id.icon_reader_theme_toggle)
        val hymnReaderRoot = view.findViewById<View>(R.id.hymn_reader_root)
        val headerPillCard = btnBack.parent?.parent as? MaterialCardView

        btnThemeToggle.setOnClickListener {
            (activity as? MainActivity)?.toggleThemeMode()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            ThemeManager.isDarkMode.collectLatest { isDark ->
                iconThemeToggle?.setImageResource(
                    if (isDark) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                )
                iconThemeToggle?.imageTintList = ColorStateList.valueOf(
                    if (isDark) 0xFFD4AF37.toInt() else ContextCompat.getColor(ctx, R.color.primary_container)
                )

                hymnReaderRoot?.background = if (isDark) {
                    ColorDrawable(0xFF000000.toInt())
                } else {
                    ContextCompat.getDrawable(ctx, R.drawable.bg_screen_gradient)
                }

                headerPillCard?.setCardBackgroundColor(if (isDark) 0xFF1C1C1E.toInt() else 0xFFFFFFFF.toInt())
                headerPillCard?.strokeColor = if (isDark) 0xFF38383A.toInt() else 0xFFEAEAEA.toInt()
                tvHeaderTitle?.setTextColor(if (isDark) 0xFFF5E6E8.toInt() else ContextCompat.getColor(ctx, R.color.primary_container))

                val lyricsTextColor = if (isDark) 0xFFF5E6E8.toInt() else ContextCompat.getColor(ctx, R.color.on_surface)
                tvVerse1?.setTextColor(lyricsTextColor)
                tvChorus?.setTextColor(lyricsTextColor)
                tvVerse2?.setTextColor(lyricsTextColor)
                tvVerse3?.setTextColor(lyricsTextColor)
                dynamicVerseTextViews.forEach { it.setTextColor(lyricsTextColor) }
            }
        }

        // Rule 5: 0ms Favorite Toggle from Reader Pill
        btnFav.setOnClickListener {
            val song = currentSong ?: return@setOnClickListener
            val isNowFav = HymnCatalog.toggleFavorite(requireContext(), song.number)
            val updated = song.copy(isFavorite = isNowFav)
            currentSong = updated
            updateFavoriteIcon(isNowFav)
            val msg = if (isNowFav) {
                "Voatahiry ao amin'ny Hira Tiako ny Fihirana #${song.numberStr}"
            } else {
                "Nesorina tao amin'ny Hira Tiako ny Fihirana #${song.numberStr}"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        btnCopy.setOnClickListener {
            val song = currentSong ?: return@setOnClickListener
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val textToCopy = "Fihirana #${song.numberStr} - ${song.title}\n\n${song.fullLyrics.ifBlank { song.verse1 }}"
            clipboard.setPrimaryClip(ClipData.newPlainText("Fihirana #${song.numberStr}", textToCopy))
            Toast.makeText(requireContext(), "Voadika ny tononkira #${song.numberStr}", Toast.LENGTH_SHORT).show()
        }

        btnShare.setOnClickListener {
            val song = currentSong ?: return@setOnClickListener
            val shareText = buildString {
                append("FIHIRANA #${song.numberStr} - ${song.title}\n")
                append("(${song.category} - JESOSY MAMONJY M. A. DAOUD)\n\n")
                append(song.fullLyrics.ifBlank { song.verse1 })
            }
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Fihirana #${song.numberStr} - ${song.title}")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(sendIntent, "Hizara ny tononkira"))
        }

        btnFontDecrease.setOnClickListener {
            currentFontSizeSp = (currentFontSizeSp - 2f).coerceAtLeast(13f)
            applyFontSize(currentFontSizeSp)
        }

        btnFontIncrease.setOnClickListener {
            currentFontSizeSp = (currentFontSizeSp + 2f).coerceAtMost(25f)
            applyFontSize(currentFontSizeSp)
        }

        btnPrevHymn.setOnClickListener {
            val prevNumber = if (currentSongNumber > 1) currentSongNumber - 1 else 830
            loadHymn(prevNumber)
        }

        btnNextHymn.setOnClickListener {
            val nextNumber = if (currentSongNumber < 830) currentSongNumber + 1 else 1
            loadHymn(nextNumber)
        }

        loadHymn(currentSongNumber)
    }

    fun loadSongByNumber(songNumber: Int, resetScroll: Boolean = true) {
        loadHymn(songNumber)
        if (resetScroll) {
            scrollView?.scrollTo(0, 0)
        }
    }

    fun loadHymn(songNumber: Int, searchQuery: String? = null) {
        currentSongNumber = songNumber
        currentSearchQuery = searchQuery?.trim()
        highlightJob?.cancel()

        if (!currentSearchQuery.isNullOrBlank() && !currentSearchQuery!!.all { it.isDigit() }) {
            isHighlightVisible = true
            highlightJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(5000L) // Exactement 5 secondes
                isHighlightVisible = false
                currentSong?.let { renderStanzas(it) }
            }
        } else {
            isHighlightVisible = false
        }

        if (!isAdded) return

        val ctx = requireContext()
        val song = HymnCatalog.getSongByNumber(ctx, songNumber)
            ?: HymnCatalog.getSongByNumber(ctx, 1)
            ?: return

        currentSong = song
        HymnCatalog.recordSongConsulted(ctx, song.number)

        tvHeaderTitle?.text = "${song.numberStr}. ${song.title}"

        val prevNum = if (song.number > 1) song.number - 1 else 830
        val nextNum = if (song.number < 830) song.number + 1 else 1
        tvPrevHymn?.text = "Hira $prevNum"
        tvNextHymn?.text = "Hira $nextNum"

        updateFavoriteIcon(song.isFavorite)
        renderStanzas(song)
        scrollView?.scrollTo(0, 0)
    }

    private fun renderStanzas(song: SongEntity) {
        val parsed = HymnCatalog.extractAllStanzas(song.fullLyrics)
        val verses: List<String> = parsed.first
        val chorus: String = parsed.second

        // Verse 1
        val v1Raw = verses.getOrNull(0).orEmpty().ifBlank { song.verse1 }
        tvVerse1?.text = HighlightHelper.highlightText(v1Raw, currentSearchQuery, isHighlightVisible)

        // Chorus (Fiv.)
        val chorusRaw = if (chorus.isNotBlank()) chorus else song.chorus
        if (chorusRaw.isNotBlank()) {
            sectionChorus?.visibility = View.VISIBLE
            tvChorus?.text = HighlightHelper.highlightText(chorusRaw, currentSearchQuery, isHighlightVisible)
        } else {
            sectionChorus?.visibility = View.GONE
        }

        // Verse 2
        val v2Raw = verses.getOrNull(1).orEmpty()
        if (v2Raw.isNotBlank()) {
            sectionVerse2?.visibility = View.VISIBLE
            tvVerse2?.text = HighlightHelper.highlightText(v2Raw, currentSearchQuery, isHighlightVisible)
        } else {
            sectionVerse2?.visibility = View.GONE
        }

        // Verse 3
        val v3Raw = verses.getOrNull(2).orEmpty()
        if (v3Raw.isNotBlank()) {
            sectionVerse3?.visibility = View.VISIBLE
            tvVerse3?.text = HighlightHelper.highlightText(v3Raw, currentSearchQuery, isHighlightVisible)
        } else {
            sectionVerse3?.visibility = View.GONE
        }

        // Extra Verses (Verse 4..N)
        val container = extraVersesContainer ?: return
        container.removeAllViews()
        dynamicVerseTextViews.clear()

        if (verses.size > 3) {
            container.visibility = View.VISIBLE
            val ctx = requireContext()
            val density = resources.displayMetrics.density
            val onSurfaceColor = ContextCompat.getColor(ctx, R.color.on_surface)
            val interFont: Typeface? = ResourcesCompat.getFont(ctx, R.font.inter)

            for (i in 3 until verses.size) {
                val stanzaText = verses[i].trim()
                if (stanzaText.isEmpty()) continue

                val tv = TextView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = (20 * density).toInt()
                    }
                    text = HighlightHelper.highlightText(stanzaText, currentSearchQuery, isHighlightVisible)
                    setTextColor(onSurfaceColor)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp)
                    setLineSpacing(0f, 1.5f)
                    if (interFont != null) {
                        typeface = interFont
                    }
                }
                container.addView(tv)
                dynamicVerseTextViews.add(tv)
            }
        } else {
            container.visibility = View.GONE
        }
    }

    private fun applyFontSize(sizeSp: Float) {
        tvVerse1?.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        tvChorus?.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        tvVerse2?.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        tvVerse3?.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        dynamicVerseTextViews.forEach { tv ->
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val icon = iconFav ?: return
        if (isFavorite) {
            icon.setImageResource(R.drawable.ic_heart)
        } else {
            icon.setImageResource(R.drawable.ic_favorite_border)
        }
    }
}
