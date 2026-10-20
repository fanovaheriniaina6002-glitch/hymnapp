package com.example.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.TraktaEntity
import com.example.data.repository.SongRepository
import com.example.ui.adapters.TraktaAdapter
import kotlinx.coroutines.launch

class TraktaFragment : Fragment() {

    private lateinit var repository: SongRepository
    private lateinit var traktaAdapter: TraktaAdapter

    private var allTraktaList: List<TraktaEntity> = emptyList()
    private var searchQuery: String = ""
    private var readerFontSizeSp: Float = 15f

    private var cardSearchBar: View? = null
    private var etSearch: EditText? = null

    private var readerModalContainer: View? = null
    private var readerScrollView: NestedScrollView? = null
    private var tvReaderBadge: TextView? = null
    private var tvReaderAuthor: TextView? = null
    private var tvReaderTitle: TextView? = null
    private var tvReaderBody: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trakta, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())

        val btnBack: View? = view.findViewById(R.id.btn_trakta_back)
        val btnSearch: View? = view.findViewById(R.id.btn_trakta_search)
        cardSearchBar = view.findViewById(R.id.card_trakta_search_bar)
        etSearch = view.findViewById(R.id.et_trakta_search)
        val rvTrakta: RecyclerView? = view.findViewById(R.id.rv_trakta_list)

        readerModalContainer = view.findViewById(R.id.trakta_reader_modal_container)
        readerScrollView = view.findViewById(R.id.trakta_reader_scroll)
        val btnReaderBack: View? = view.findViewById(R.id.btn_trakta_reader_back)
        val btnReaderFont: View? = view.findViewById(R.id.btn_trakta_reader_font)
        tvReaderBadge = view.findViewById(R.id.tv_trakta_reader_badge)
        tvReaderAuthor = view.findViewById(R.id.tv_trakta_reader_author)
        tvReaderTitle = view.findViewById(R.id.tv_trakta_reader_title)
        tvReaderBody = view.findViewById(R.id.tv_trakta_reader_body)

        btnBack?.setOnClickListener {
            (activity as? MainActivity)?.closeTrakta()
        }

        btnSearch?.setOnClickListener {
            val isVisible = cardSearchBar?.visibility == View.VISIBLE
            if (isVisible) {
                cardSearchBar?.visibility = View.GONE
                etSearch?.setText("")
            } else {
                cardSearchBar?.visibility = View.VISIBLE
                etSearch?.requestFocus()
            }
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                renderFilteredTrakta()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnReaderBack?.setOnClickListener {
            closeTraktaReader()
        }

        btnReaderFont?.setOnClickListener {
            readerFontSizeSp = when {
                readerFontSizeSp < 17f -> 18f
                readerFontSizeSp < 20f -> 21f
                else -> 15f
            }
            tvReaderBody?.setTextSize(TypedValue.COMPLEX_UNIT_SP, readerFontSizeSp)
        }

        // Clicking "Hamaky" (or any Trakta card) immediately navigates to TraktaReaderScreen
        traktaAdapter = TraktaAdapter { clickedTrakta ->
            (activity as? MainActivity)?.openTraktaReaderScreen(clickedTrakta)
        }
        rvTrakta?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            itemAnimator = null // 0% blur / 60 FPS rendering
            adapter = traktaAdapter
        }

        // Observe Offline Room Trakta Items
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allTraktaItems.collect { items ->
                    allTraktaList = items
                    renderFilteredTrakta()
                }
            }
        }
    }

    private fun renderFilteredTrakta() {
        val filtered = if (searchQuery.isEmpty()) {
            allTraktaList
        } else {
            allTraktaList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.numberLabel.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
            }
        }
        traktaAdapter.submitList(filtered)
    }

    fun openTraktaReader(trakta: TraktaEntity) {
        tvReaderBadge?.text = "TRAKTA ${trakta.numberLabel}"
        tvReaderAuthor?.text = "${trakta.author} • 100% OFFLINE"
        tvReaderTitle?.text = trakta.title
        tvReaderBody?.text = trakta.content
        readerScrollView?.scrollTo(0, 0)
        readerModalContainer?.visibility = View.VISIBLE
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }

    fun closeTraktaReader() {
        readerModalContainer?.visibility = View.GONE
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    fun isTraktaReaderVisible(): Boolean = readerModalContainer?.visibility == View.VISIBLE
}
