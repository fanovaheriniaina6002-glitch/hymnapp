package com.example.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.TraktaEntity
import com.example.data.repository.SongRepository
import com.example.ui.util.HighlightHelper
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TraktaReaderFragment : Fragment() {

    private lateinit var repository: SongRepository

    private var allTraktas: List<TraktaEntity> = emptyList()
    private var currentTraktaId: Int = 1
    private var currentTrakta: TraktaEntity? = null
    private var bodyFontSizeSp: Float = 15.5f

    private var currentHighlightQuery: String? = null
    private var isHighlightVisible: Boolean = false
    private var highlightJob: Job? = null

    private var scrollView: NestedScrollView? = null
    private var cardCover: MaterialCardView? = null
    private var ivCover: ImageView? = null
    private var tvTopSubtitle: TextView? = null
    private var tvNumber: TextView? = null
    private var tvTitle: TextView? = null
    private var tvAuthor: TextView? = null
    private var tvBody: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trakta_reader, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())

        val btnBack: View? = view.findViewById(R.id.btn_trakta_reader_back)
        val btnFont: View? = view.findViewById(R.id.btn_trakta_reader_font)
        val btnPrev: View? = view.findViewById(R.id.btn_reader_prev_trakta)
        val btnNext: View? = view.findViewById(R.id.btn_reader_next_trakta)
        val btnShare: View? = view.findViewById(R.id.btn_reader_share_trakta)

        scrollView = view.findViewById(R.id.trakta_reader_scroll_view)
        cardCover = view.findViewById(R.id.card_reader_cover)
        ivCover = view.findViewById(R.id.iv_reader_cover)
        tvTopSubtitle = view.findViewById(R.id.tv_trakta_reader_top_subtitle)
        tvNumber = view.findViewById(R.id.tv_reader_number)
        tvTitle = view.findViewById(R.id.tv_reader_title)
        tvAuthor = view.findViewById(R.id.tv_reader_author)
        tvBody = view.findViewById(R.id.tv_reader_body)

        btnBack?.setOnClickListener {
            (activity as? MainActivity)?.closeTraktaReaderScreen()
        }

        btnFont?.setOnClickListener {
            bodyFontSizeSp = when {
                bodyFontSizeSp < 17f -> 18f
                bodyFontSizeSp < 20f -> 21f
                else -> 15.5f
            }
            tvBody?.setTextSize(TypedValue.COMPLEX_UNIT_SP, bodyFontSizeSp)
        }

        btnPrev?.setOnClickListener {
            if (allTraktas.isNotEmpty()) {
                val idx = allTraktas.indexOfFirst { it.id == currentTraktaId }
                val prevIdx = if (idx > 0) idx - 1 else allTraktas.lastIndex
                loadTrakta(allTraktas[prevIdx], resetScroll = true)
            }
        }

        btnNext?.setOnClickListener {
            if (allTraktas.isNotEmpty()) {
                val idx = allTraktas.indexOfFirst { it.id == currentTraktaId }
                val nextIdx = if (idx in 0 until allTraktas.lastIndex) idx + 1 else 0
                loadTrakta(allTraktas[nextIdx], resetScroll = true)
            }
        }

        btnShare?.setOnClickListener {
            val item = currentTrakta ?: return@setOnClickListener
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "${item.numberLabel} - ${item.title}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${item.numberLabel} • ${item.title}\nMpanoratra: ${item.author}\n\n${item.content}"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "Hizara ny Trakta"))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allTraktaItems.collect { items ->
                    allTraktas = items
                    val target = currentTrakta
                        ?: items.firstOrNull { it.id == currentTraktaId }
                        ?: items.firstOrNull()
                    if (target != null) {
                        bindTraktaViews(target, resetScroll = false)
                    }
                }
            }
        }
    }

    fun loadTrakta(
        trakta: TraktaEntity,
        resetScroll: Boolean = true,
        searchQuery: String? = null
    ) {
        currentTraktaId = trakta.id
        currentTrakta = trakta
        currentHighlightQuery = searchQuery?.trim()
        highlightJob?.cancel()

        if (!currentHighlightQuery.isNullOrBlank() && !currentHighlightQuery!!.all { it.isDigit() }) {
            isHighlightVisible = true
            highlightJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(5000L) // Exactement 5 secondes
                isHighlightVisible = false
                currentTrakta?.let { bindTraktaViews(it, resetScroll = false) }
            }
        } else {
            isHighlightVisible = false
        }

        bindTraktaViews(trakta, resetScroll)
    }

    private fun bindTraktaViews(trakta: TraktaEntity, resetScroll: Boolean) {
        currentTrakta = trakta
        currentTraktaId = trakta.id

        tvTopSubtitle?.text = "TRAKTA ${trakta.numberLabel} • ${trakta.author}"
        tvNumber?.text = trakta.numberLabel
        tvTitle?.text = HighlightHelper.highlightText(trakta.title, currentHighlightQuery, isHighlightVisible)
        tvAuthor?.text = "Mpanoratra: ${trakta.author}"
        tvBody?.text = HighlightHelper.highlightText(trakta.content, currentHighlightQuery, isHighlightVisible)

        runCatching {
            cardCover?.setCardBackgroundColor(Color.parseColor(trakta.coverBgHex))
        }

        val coverDrawable = when (trakta.coverKey) {
            "trakta_1" -> R.drawable.ic_trakta_cover_1
            "trakta_2" -> R.drawable.ic_trakta_cover_2
            "trakta_3" -> R.drawable.ic_trakta_cover_3
            "trakta_4" -> R.drawable.ic_trakta_cover_4
            else -> R.drawable.ic_trakta_cover_1
        }
        ivCover?.setImageResource(coverDrawable)

        if (resetScroll) {
            scrollView?.scrollTo(0, 0)
        }
    }
}
