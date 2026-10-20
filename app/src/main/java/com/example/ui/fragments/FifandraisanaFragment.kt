package com.example.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.SongRepository
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class FifandraisanaFragment : Fragment() {

    private lateinit var repository: SongRepository
    private var selectedTopic: String = "Fanamarihana"

    private var chipFanamarihana: MaterialCardView? = null
    private var chipFiarahaMiasa: MaterialCardView? = null
    private var chipFanampiana: MaterialCardView? = null
    private var chipFisaorana: MaterialCardView? = null

    private var tvFanamarihana: TextView? = null
    private var tvFiarahaMiasa: TextView? = null
    private var tvFanampiana: TextView? = null
    private var tvFisaorana: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fifandraisana, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())

        val btnBack: View? = view.findViewById(R.id.btn_fifandraisana_back)
        val btnInfo: View? = view.findViewById(R.id.btn_fifandraisana_info)

        val cardEmail: View? = view.findViewById(R.id.card_fifandraisana_email)
        val btnCopyEmail: View? = view.findViewById(R.id.btn_copy_email)
        val tvEmailValue: TextView? = view.findViewById(R.id.tv_fifandraisana_email_value)

        val cardPhone: View? = view.findViewById(R.id.card_fifandraisana_phone)
        val btnCopyPhone: View? = view.findViewById(R.id.btn_copy_phone)
        val tvPhoneValue: TextView? = view.findViewById(R.id.tv_fifandraisana_phone_value)

        val cardLocation: View? = view.findViewById(R.id.card_fifandraisana_location)

        chipFanamarihana = view.findViewById(R.id.chip_topic_fanamarihana)
        chipFiarahaMiasa = view.findViewById(R.id.chip_topic_fiaraha_miasa)
        chipFanampiana = view.findViewById(R.id.chip_topic_fanampiana)
        chipFisaorana = view.findViewById(R.id.chip_topic_fisaorana)

        tvFanamarihana = view.findViewById(R.id.tv_topic_fanamarihana)
        tvFiarahaMiasa = view.findViewById(R.id.tv_topic_fiaraha_miasa)
        tvFanampiana = view.findViewById(R.id.tv_topic_fanampiana)
        tvFisaorana = view.findViewById(R.id.tv_topic_fisaorana)

        val etName: EditText? = view.findViewById(R.id.et_fifandraisana_name)
        val etContact: EditText? = view.findViewById(R.id.et_fifandraisana_contact)
        val etMessage: EditText? = view.findViewById(R.id.et_fifandraisana_message)
        val cardSentBanner: View? = view.findViewById(R.id.card_fifandraisana_sent_banner)
        val tvSentStatus: TextView? = view.findViewById(R.id.tv_fifandraisana_sent_status)
        val btnSend: View? = view.findViewById(R.id.btn_fifandraisana_send)

        btnBack?.setOnClickListener {
            (activity as? MainActivity)?.closeFifandraisana()
        }

        btnInfo?.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Fifandraisana & Fanampiana")
                .setMessage(
                    "Jesosy Mamonjy M. A. Daoud — Application 100% Offline\n\n" +
                        "Foibe: Ambohipo, Antananarivo\n" +
                        "Faritra: Fianarantsoa, Mahajanga, Toamasina, Antsiranana\n\n" +
                        "Azonao atao ny mandefa fanamarihana na soso-kevitra eto amin'ity pejy ity."
                )
                .setPositiveButton("Hidio", null)
                .show()
        }

        val emailAddress = tvEmailValue?.text?.toString() ?: "fifandraisana@jesosymamonjy.mg"
        val phoneNumber = tvPhoneValue?.text?.toString() ?: "+261 34 05 123 45"

        btnCopyEmail?.setOnClickListener {
            copyToClipboard("Mailaka", emailAddress)
        }

        cardEmail?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$emailAddress")
                    putExtra(Intent.EXTRA_SUBJECT, "[$selectedTopic] Jesosy Mamonjy M. A. Daoud")
                }
                startActivity(intent)
            } catch (_: Exception) {
                copyToClipboard("Mailaka", emailAddress)
            }
        }

        btnCopyPhone?.setOnClickListener {
            copyToClipboard("Telefaonina", phoneNumber)
        }

        cardPhone?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.replace(" ", "")}"))
                startActivity(intent)
            } catch (_: Exception) {
                copyToClipboard("Telefaonina", phoneNumber)
            }
        }

        cardLocation?.setOnClickListener {
            (activity as? MainActivity)?.openFandaharampotoana()
        }

        // Topic Selection Chips
        chipFanamarihana?.setOnClickListener {
            selectedTopic = "Fanamarihana"
            updateTopicChipVisuals()
        }
        chipFiarahaMiasa?.setOnClickListener {
            selectedTopic = "Fiaraha-miasa"
            updateTopicChipVisuals()
        }
        chipFanampiana?.setOnClickListener {
            selectedTopic = "Fanampiana Teknika"
            updateTopicChipVisuals()
        }
        chipFisaorana?.setOnClickListener {
            selectedTopic = "Fisaorana"
            updateTopicChipVisuals()
        }

        // Send Message Handler
        btnSend?.setOnClickListener {
            val senderName = etName?.text?.toString()?.trim().orEmpty().ifEmpty { "Mpino" }
            val senderContact = etContact?.text?.toString()?.trim().orEmpty()
            val messageBody = etMessage?.text?.toString()?.trim().orEmpty()

            if (messageBody.isEmpty()) {
                etMessage?.error = "Soraty aloha ny hafatrao azafady"
                etMessage?.requestFocus()
                return@setOnClickListener
            }

            // Save locally in SharedPreferences (100% Offline persistence)
            val prefs = requireContext().getSharedPreferences(
                "jesosy_mamonjy_fifandraisana_prefs",
                Context.MODE_PRIVATE
            )
            val count = prefs.getInt("saved_messages_count", 0) + 1
            prefs.edit()
                .putInt("saved_messages_count", count)
                .putString("last_sender_name", senderName)
                .putString("last_sender_contact", senderContact)
                .putString("last_topic", selectedTopic)
                .putString("last_message", messageBody)
                .apply()

            tvSentStatus?.text =
                "Voaray sy voatahiry ny hafatrao ($selectedTopic). Misaotra indrindra, $senderName!"
            cardSentBanner?.visibility = View.VISIBLE
            etMessage?.setText("")

            Toast.makeText(
                requireContext(),
                "Voatahiry soa aman-tsara ny hafatrao!",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Pre-fill user's name from Room profile
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.userProfile.collect { profile ->
                    if (profile != null && etName?.text.isNullOrBlank()) {
                        etName?.setText(profile.displayName)
                    }
                }
            }
        }
    }

    private fun copyToClipboard(label: String, value: String) {
        val ctx = context ?: return
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(ctx, "Voaray: $value", Toast.LENGTH_SHORT).show()
    }

    private fun updateTopicChipVisuals() {
        val ctx = context ?: return
        val grenat = 0xFF800020.toInt()
        val inactiveBg = 0xFFFAF8F9.toInt()
        val inactiveBorder = ContextCompat.getColor(ctx, R.color.border_eae0e2)
        val white = ContextCompat.getColor(ctx, R.color.white)
        val inactiveText = 0xFF625E55.toInt()

        data class TopicChip(
            val topic: String,
            val card: MaterialCardView?,
            val label: TextView?
        )

        val chips = listOf(
            TopicChip("Fanamarihana", chipFanamarihana, tvFanamarihana),
            TopicChip("Fiaraha-miasa", chipFiarahaMiasa, tvFiarahaMiasa),
            TopicChip("Fanampiana Teknika", chipFanampiana, tvFanampiana),
            TopicChip("Fisaorana", chipFisaorana, tvFisaorana)
        )

        chips.forEach { item ->
            val isSelected = item.topic == selectedTopic
            item.card?.setCardBackgroundColor(if (isSelected) grenat else inactiveBg)
            item.card?.strokeColor = if (isSelected) grenat else inactiveBorder
            item.label?.setTextColor(if (isSelected) white else inactiveText)
        }
    }
}
