package com.example.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.OnboardingPreferencesManager
import com.example.data.local.UserProfileEntity
import com.example.data.repository.SongRepository
import com.example.ui.onboarding.ONBOARDING_REGIONS
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class KaontyFragment : Fragment() {

    private lateinit var repository: SongRepository
    private lateinit var onboardingPrefs: OnboardingPreferencesManager
    private var currentProfile = UserProfileEntity()
    private var selectedGender = "male"
    private var pendingPhotoUri: String = ""

    private var profileModalContainer: View? = null
    private var inputProfileName: EditText? = null
    private var selectProfileRegion: Spinner? = null

    private var ivKaontyTopAvatar: ImageView? = null
    private var ivKaontyProfileAvatar: ImageView? = null
    private var ivModalProfileAvatar: ImageView? = null

    private var btnGenderMale: MaterialCardView? = null
    private var btnGenderFemale: MaterialCardView? = null
    private var iconGenderMale: ImageView? = null
    private var iconGenderFemale: ImageView? = null
    private var tvGenderMale: TextView? = null
    private var tvGenderFemale: TextView? = null

    private var btnThemeJour: MaterialCardView? = null
    private var btnThemeNuit: MaterialCardView? = null
    private var btnThemeAuto: MaterialCardView? = null
    private var iconThemeJour: ImageView? = null
    private var iconThemeNuit: ImageView? = null
    private var iconThemeAuto: ImageView? = null
    private var tvThemeJour: TextView? = null
    private var tvThemeNuit: TextView? = null
    private var tvThemeAuto: TextView? = null

    private val regions = ONBOARDING_REGIONS

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            pendingPhotoUri = uri.toString()
            bindAvatarImage(ivModalProfileAvatar, pendingPhotoUri, R.drawable.ic_menu_book, 12)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_kaonty, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())
        repository = SongRepository(db.songDao())
        onboardingPrefs = OnboardingPreferencesManager(requireContext())

        // 1. Top Pill Views
        val btnTopPerson: View? = view.findViewById(R.id.btn_kaonty_top_person)
        val btnTopTheme: View? = view.findViewById(R.id.btn_kaonty_top_theme)
        ivKaontyTopAvatar = view.findViewById(R.id.iv_kaonty_top_avatar)

        // 3. Profile Card Views
        val cardUserProfile: View? = view.findViewById(R.id.card_user_profile)
        val btnEditProfile: View? = view.findViewById(R.id.btn_edit_profile)
        val tvDisplayName: TextView? = view.findViewById(R.id.profile_display_name)
        val tvDisplaySubtext: TextView? = view.findViewById(R.id.profile_display_subtext)
        ivKaontyProfileAvatar = view.findViewById(R.id.iv_kaonty_profile_avatar)

        // 4. Photo Category Cards (Trakta, Fotoana, Rakitsary)
        val cardTrakta: View? = view.findViewById(R.id.card_kaonty_trakta)
        val cardFotoana: View? = view.findViewById(R.id.card_kaonty_fotoana)
        val cardRakitsary: View? = view.findViewById(R.id.card_kaonty_rakitsary)

        // 5. Settings ("Paramètre") Views
        btnThemeJour = view.findViewById(R.id.btn_theme_jour)
        btnThemeNuit = view.findViewById(R.id.btn_theme_nuit)
        btnThemeAuto = view.findViewById(R.id.btn_theme_auto)
        iconThemeJour = view.findViewById(R.id.icon_theme_jour)
        iconThemeNuit = view.findViewById(R.id.icon_theme_nuit)
        iconThemeAuto = view.findViewById(R.id.icon_theme_auto)
        tvThemeJour = view.findViewById(R.id.tv_theme_jour)
        tvThemeNuit = view.findViewById(R.id.tv_theme_nuit)
        tvThemeAuto = view.findViewById(R.id.tv_theme_auto)

        val tvFontSizeBadge: TextView? = view.findViewById(R.id.tv_font_size_badge)
        val btnFontMinus: View? = view.findViewById(R.id.btn_font_minus)
        val btnFontPlus: View? = view.findViewById(R.id.btn_font_plus)
        val seekBarFontSize: SeekBar? = view.findViewById(R.id.seekbar_font_size)
        val tvLyricsLivePreview: TextView? = view.findViewById(R.id.tv_lyrics_live_preview)

        // 6. Contact Support Card
        val cardContactSupport: View? = view.findViewById(R.id.card_contact_support)

        // 7. Edit Profile Modal / Bottom Sheet Views
        profileModalContainer = view.findViewById(R.id.profile_modal_container)
        val profileModalBackdrop: View? = view.findViewById(R.id.profile_modal_backdrop)
        val profileModalCloseBtn: View? = view.findViewById(R.id.profile_modal_close_btn)
        val btnChangePhoto: View? = view.findViewById(R.id.btn_change_photo)
        ivModalProfileAvatar = view.findViewById(R.id.iv_modal_profile_avatar)
        inputProfileName = view.findViewById(R.id.input_profile_name)
        selectProfileRegion = view.findViewById(R.id.select_profile_region)

        btnGenderMale = view.findViewById(R.id.btn_gender_male)
        btnGenderFemale = view.findViewById(R.id.btn_gender_female)
        iconGenderMale = view.findViewById(R.id.icon_gender_male)
        iconGenderFemale = view.findViewById(R.id.icon_gender_female)
        tvGenderMale = view.findViewById(R.id.tv_gender_male)
        tvGenderFemale = view.findViewById(R.id.tv_gender_female)

        val btnSaveProfile: View? = view.findViewById(R.id.btn_save_profile)
        val btnCancelProfile: View? = view.findViewById(R.id.btn_cancel_profile)

        // Setup Region Spinner (6 church regions)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            regions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        selectProfileRegion?.adapter = spinnerAdapter
        selectProfileRegion?.setSelection(0)

        // Open Edit Profile Modal
        val openModalListener = View.OnClickListener { openProfileModal() }
        btnEditProfile?.setOnClickListener(openModalListener)
        cardUserProfile?.setOnClickListener(openModalListener)
        btnTopPerson?.setOnClickListener(openModalListener)

        // Close Edit Profile Modal
        val closeModalListener = View.OnClickListener { closeProfileModal() }
        profileModalBackdrop?.setOnClickListener(closeModalListener)
        profileModalCloseBtn?.setOnClickListener(closeModalListener)
        btnCancelProfile?.setOnClickListener(closeModalListener)

        btnChangePhoto?.setOnClickListener {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        // Gender Selection Buttons
        btnGenderMale?.setOnClickListener {
            selectedGender = "male"
            updateGenderVisuals("male")
        }
        btnGenderFemale?.setOnClickListener {
            selectedGender = "female"
            updateGenderVisuals("female")
        }

        // Save Profile Changes to Offline Room Database + DataStore/SharedPreferences
        btnSaveProfile?.setOnClickListener {
            val newName = inputProfileName?.text?.toString()?.trim()
                ?.takeIf { it.isNotEmpty() } ?: currentProfile.displayName
            val selectedRegionIndex = selectProfileRegion?.selectedItemPosition ?: 0
            val newRegion = regions.getOrElse(selectedRegionIndex) { regions.first() }

            val updated = currentProfile.copy(
                displayName = newName,
                gender = selectedGender,
                region = newRegion,
                photoUri = pendingPhotoUri,
                isOnboardingCompleted = true
            )
            viewLifecycleOwner.lifecycleScope.launch {
                repository.saveUserProfile(updated)
                onboardingPrefs.saveOnboardingProfile(
                    name = newName,
                    gender = if (selectedGender == "female") "Vehivavy" else "Lehilahy",
                    region = newRegion,
                    imageUri = pendingPhotoUri,
                    songDao = AppDatabase.getInstance(requireContext()).songDao()
                )
                closeProfileModal()
            }
        }

        // 3 Photo Category Cards Click Listeners
        cardTrakta?.setOnClickListener {
            (activity as? com.example.MainActivity)?.openTrakta()
        }
        cardFotoana?.setOnClickListener {
            (activity as? com.example.MainActivity)?.openFandaharampotoana()
        }
        cardRakitsary?.setOnClickListener {
            (activity as? com.example.MainActivity)?.openRakitsary()
        }

        // Theme Segmented Buttons (Jour / Nuit / Auto)
        val applyThemeMode: (String) -> Unit = { mode ->
            updateThemeVisuals(mode)
            val updated = currentProfile.copy(themeMode = mode)
            currentProfile = updated
            viewLifecycleOwner.lifecycleScope.launch {
                repository.saveUserProfile(updated)
                (activity as? com.example.MainActivity)?.setThemeMode(mode)
            }
        }
        btnThemeJour?.setOnClickListener { applyThemeMode("jour") }
        btnThemeNuit?.setOnClickListener { applyThemeMode("nuit") }
        btnThemeAuto?.setOnClickListener { applyThemeMode("auto") }
        btnTopTheme?.setOnClickListener {
            val nextMode = if (currentProfile.themeMode == "jour") "nuit" else "jour"
            applyThemeMode(nextMode)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                com.example.ui.theme.ThemeManager.themeMode.collect { mode ->
                    updateThemeVisuals(mode)
                }
            }
        }

        // Font Size Slider & +/- Buttons (range 12 sp .. 26 sp)
        val applyFontSize: (Int) -> Unit = { sizeSp ->
            val clamped = sizeSp.coerceIn(12, 26)
            val progress = clamped - 12
            if (seekBarFontSize?.progress != progress) {
                seekBarFontSize?.progress = progress
            }
            tvFontSizeBadge?.text = "$clamped sp"
            tvLyricsLivePreview?.setTextSize(TypedValue.COMPLEX_UNIT_SP, (clamped - 3).toFloat().coerceAtLeast(11f))

            if (currentProfile.fontSizeSp != clamped) {
                val updated = currentProfile.copy(fontSizeSp = clamped)
                currentProfile = updated
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.saveUserProfile(updated)
                }
            }
        }

        seekBarFontSize?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val sizeSp = (progress + 12).coerceIn(12, 26)
                tvFontSizeBadge?.text = "$sizeSp sp"
                tvLyricsLivePreview?.setTextSize(TypedValue.COMPLEX_UNIT_SP, (sizeSp - 3).toFloat().coerceAtLeast(11f))
                if (fromUser) {
                    val updated = currentProfile.copy(fontSizeSp = sizeSp)
                    currentProfile = updated
                    viewLifecycleOwner.lifecycleScope.launch {
                        repository.saveUserProfile(updated)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        btnFontMinus?.setOnClickListener {
            applyFontSize(currentProfile.fontSizeSp - 1)
        }
        btnFontPlus?.setOnClickListener {
            applyFontSize(currentProfile.fontSizeSp + 1)
        }

        // Contact Support Bento Card ("Hifandray aminay" -> Fifandraisana Screen)
        cardContactSupport?.setOnClickListener {
            (activity as? com.example.MainActivity)?.openFifandraisana()
        }

        // Observe UserProfile from Offline Room Database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.userProfile.collect { profile ->
                    val resolved = profile ?: UserProfileEntity()
                    currentProfile = resolved
                    selectedGender = resolved.gender
                    pendingPhotoUri = resolved.photoUri

                    val genderPrefix = if (resolved.gender == "female") "Ranabavy" else "Rahalahy"
                    tvDisplayName?.text = resolved.displayName
                    tvDisplaySubtext?.text = "$genderPrefix • Jesosy Mamonjy M.A Daoud\n${resolved.region}"

                    bindAvatarImage(ivKaontyTopAvatar, resolved.photoUri, R.drawable.ic_person, 6)
                    bindAvatarImage(ivKaontyProfileAvatar, resolved.photoUri, R.drawable.ic_menu_book, 12)
                    bindAvatarImage(ivModalProfileAvatar, resolved.photoUri, R.drawable.ic_menu_book, 12)

                    updateThemeVisuals(resolved.themeMode)
                    val clampedFont = resolved.fontSizeSp.coerceIn(12, 26)
                    seekBarFontSize?.progress = clampedFont - 12
                    tvFontSizeBadge?.text = "$clampedFont sp"
                    tvLyricsLivePreview?.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        (clampedFont - 3).toFloat().coerceAtLeast(11f)
                    )
                }
            }
        }
    }

    private fun bindAvatarImage(
        imageView: ImageView?,
        photoUri: String,
        fallbackResId: Int,
        fallbackPaddingDp: Int
    ) {
        val iv = imageView ?: return
        val ctx = context ?: return
        if (photoUri.isNotBlank()) {
            try {
                iv.setPadding(0, 0, 0, 0)
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                ImageViewCompat.setImageTintList(iv, null)
                iv.setImageURI(Uri.parse(photoUri))
                return
            } catch (_: Exception) {
            }
        }
        val padPx = (fallbackPaddingDp * resources.displayMetrics.density).toInt()
        iv.setPadding(padPx, padPx, padPx, padPx)
        iv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        iv.setImageResource(fallbackResId)
        ImageViewCompat.setImageTintList(
            iv,
            ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.primary_container))
        )
    }

    private fun updateThemeVisuals(activeMode: String) {
        val ctx = context ?: return
        val white = ContextCompat.getColor(ctx, R.color.white)
        val transparent = ContextCompat.getColor(ctx, android.R.color.transparent)
        val grenat = 0xFF800020.toInt()
        val primaryContainer = ContextCompat.getColor(ctx, R.color.primary_container)
        val inactiveColor = ContextCompat.getColor(ctx, R.color.on_surface_variant)
        val dp1 = (1 * resources.displayMetrics.density).toInt()

        data class ThemeTab(
            val mode: String,
            val card: MaterialCardView?,
            val icon: ImageView?,
            val label: TextView?
        )

        val tabs = listOf(
            ThemeTab("jour", btnThemeJour, iconThemeJour, tvThemeJour),
            ThemeTab("nuit", btnThemeNuit, iconThemeNuit, tvThemeNuit),
            ThemeTab("auto", btnThemeAuto, iconThemeAuto, tvThemeAuto)
        )

        tabs.forEach { tab ->
            val isSelected = tab.mode == activeMode
            tab.card?.setCardBackgroundColor(if (isSelected) white else transparent)
            tab.card?.strokeWidth = if (isSelected) dp1 else 0
            val iconTint = if (isSelected) grenat else inactiveColor
            val textTint = if (isSelected) primaryContainer else inactiveColor
            tab.icon?.let { ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(iconTint)) }
            tab.label?.setTextColor(textTint)
        }
    }

    private fun updateGenderVisuals(gender: String) {
        val ctx = context ?: return
        val grenat = 0xFF800020.toInt()
        val white = ContextCompat.getColor(ctx, R.color.white)
        val transparent = ContextCompat.getColor(ctx, android.R.color.transparent)
        val inactiveText = ContextCompat.getColor(ctx, R.color.on_surface_variant)

        val isMale = gender == "male"

        btnGenderMale?.setCardBackgroundColor(if (isMale) grenat else transparent)
        btnGenderMale?.strokeColor = if (isMale) grenat else transparent
        iconGenderMale?.let {
            ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(if (isMale) white else inactiveText))
        }
        tvGenderMale?.setTextColor(if (isMale) white else inactiveText)

        btnGenderFemale?.setCardBackgroundColor(if (!isMale) grenat else transparent)
        btnGenderFemale?.strokeColor = if (!isMale) grenat else transparent
        iconGenderFemale?.let {
            ImageViewCompat.setImageTintList(it, ColorStateList.valueOf(if (!isMale) white else inactiveText))
        }
        tvGenderFemale?.setTextColor(if (!isMale) white else inactiveText)
    }

    fun openProfileModal() {
        inputProfileName?.setText(currentProfile.displayName)
        inputProfileName?.setSelection(currentProfile.displayName.length)
        val regionIdx = regions.indexOfFirst { it.equals(currentProfile.region, ignoreCase = true) }
            .coerceAtLeast(0)
        selectProfileRegion?.setSelection(regionIdx)
        selectedGender = currentProfile.gender
        pendingPhotoUri = currentProfile.photoUri
        bindAvatarImage(ivModalProfileAvatar, pendingPhotoUri, R.drawable.ic_menu_book, 12)
        updateGenderVisuals(selectedGender)
        profileModalContainer?.visibility = View.VISIBLE
    }

    fun closeProfileModal() {
        profileModalContainer?.visibility = View.GONE
    }

    fun isProfileModalVisible(): Boolean = profileModalContainer?.visibility == View.VISIBLE
}
