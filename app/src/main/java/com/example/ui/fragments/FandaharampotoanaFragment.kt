package com.example.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ScheduleItemEntity
import com.example.data.repository.SongRepository
import kotlinx.coroutines.launch

class FandaharampotoanaFragment : Fragment() {

    private lateinit var repository: SongRepository
    private var scheduleItems: List<ScheduleItemEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fandaharampotoana, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())

        val btnBack: View? = view.findViewById(R.id.btn_fotoana_back)
        val btnCalendar: View? = view.findViewById(R.id.btn_fotoana_calendar)
        val cardVerse: View? = view.findViewById(R.id.card_fotoana_verse)

        val cardAlahady1: View? = view.findViewById(R.id.card_alahady_1)
        val cardAlahady2: View? = view.findViewById(R.id.card_alahady_2)
        val cardAlahady3: View? = view.findViewById(R.id.card_alahady_3)
        val cardAndavanandro1: View? = view.findViewById(R.id.card_andavanandro_1)
        val cardAndavanandro2: View? = view.findViewById(R.id.card_andavanandro_2)

        val tvTitle1: TextView? = view.findViewById(R.id.tv_schedule_title_1)
        val tvSub1: TextView? = view.findViewById(R.id.tv_schedule_sub_1)
        val tvTime1: TextView? = view.findViewById(R.id.tv_schedule_time_1)

        val tvTitle2: TextView? = view.findViewById(R.id.tv_schedule_title_2)
        val tvSub2: TextView? = view.findViewById(R.id.tv_schedule_sub_2)
        val tvTime2: TextView? = view.findViewById(R.id.tv_schedule_time_2)

        val tvTitle3: TextView? = view.findViewById(R.id.tv_schedule_title_3)
        val tvSub3: TextView? = view.findViewById(R.id.tv_schedule_sub_3)
        val tvTime3: TextView? = view.findViewById(R.id.tv_schedule_time_3)

        val tvTitle4: TextView? = view.findViewById(R.id.tv_schedule_title_4)
        val tvSub4: TextView? = view.findViewById(R.id.tv_schedule_sub_4)
        val tvTime4: TextView? = view.findViewById(R.id.tv_schedule_time_4)

        val tvTitle5: TextView? = view.findViewById(R.id.tv_schedule_title_5)
        val tvSub5: TextView? = view.findViewById(R.id.tv_schedule_sub_5)
        val tvTime5: TextView? = view.findViewById(R.id.tv_schedule_time_5)

        btnBack?.setOnClickListener {
            (activity as? MainActivity)?.closeFandaharampotoana()
        }

        btnCalendar?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Fandaharampotoana 100% Offline • Jesosy Mamonjy M. A. Daoud",
                Toast.LENGTH_SHORT
            ).show()
        }

        cardVerse?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Hebreo 10:25")
                .setMessage(
                    "« Aza mahafoy ny fiarahantsika miangona, tahaka ny fanaon'ny sasany, fa mifananara kosa, mainka fa hitanareo fa mihantomotra ny andro. »"
                )
                .setPositiveButton("Amena", null)
                .show()
        }

        val showScheduleDetails: (Int, String, String, String) -> Unit =
            { id, fallbackTitle, fallbackSub, fallbackTime ->
                val item = scheduleItems.firstOrNull { it.id == id }
                val title = item?.title ?: fallbackTitle
                val time = item?.timeRange ?: fallbackTime
                val details = item?.details ?: fallbackSub
                AlertDialog.Builder(requireContext())
                    .setTitle("$title ($time)")
                    .setMessage(details)
                    .setPositiveButton("Hikaroka Hira") { _, _ ->
                        (activity as? MainActivity)?.switchToTab(MainActivity.TAB_HIKAROKA)
                    }
                    .setNegativeButton("Hidio", null)
                    .show()
            }

        cardAlahady1?.setOnClickListener {
            showScheduleDetails(1, "Sekoly Alahady & Tanora", "Fianarana Soratra Masina", "08h30 – 09h30")
        }
        cardAlahady2?.setOnClickListener {
            showScheduleDetails(2, "Maraina", "Hira, Vavaka sy Toriteny", "09h30 – 11h00")
        }
        cardAlahady3?.setOnClickListener {
            showScheduleDetails(3, "Hariva", "Hira, Vavaka sy Toriteny", "14h00 – 15h00")
        }
        cardAndavanandro1?.setOnClickListener {
            showScheduleDetails(4, "Vavaka Maraina", "Talata – Zoma", "05h30 – 06h00")
        }
        cardAndavanandro2?.setOnClickListener {
            showScheduleDetails(5, "Fivoriana", "Alarobia hariva", "18h00 – 19h30")
        }

        // Observe Offline Room Schedule Items
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allScheduleItems.collect { items ->
                    scheduleItems = items
                    items.forEach { item ->
                        when (item.id) {
                            1 -> {
                                tvTitle1?.text = item.title
                                tvSub1?.text = item.subtitle
                                tvTime1?.text = item.timeRange
                            }
                            2 -> {
                                tvTitle2?.text = item.title
                                tvSub2?.text = item.subtitle
                                tvTime2?.text = item.timeRange
                            }
                            3 -> {
                                tvTitle3?.text = item.title
                                tvSub3?.text = item.subtitle
                                tvTime3?.text = item.timeRange
                            }
                            4 -> {
                                tvTitle4?.text = item.title
                                tvSub4?.text = item.subtitle
                                tvTime4?.text = item.timeRange
                            }
                            5 -> {
                                tvTitle5?.text = item.title
                                tvSub5?.text = item.subtitle
                                tvTime5?.text = item.timeRange
                            }
                        }
                    }
                }
            }
        }
    }
}
