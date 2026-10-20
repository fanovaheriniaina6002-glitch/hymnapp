package com.example

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    @Test
    fun `launch MainActivity and navigate across all screens without crashing`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).create().start().resume().visible()
        val activity = controller.get()
        assertNotNull(activity)

        // Test navigating across all tabs
        activity.switchToTab(MainActivity.TAB_FANDRAISANA)
        activity.switchToTab(MainActivity.TAB_HIKAROKA)
        activity.switchToTab(MainActivity.TAB_HIRA_TIAKO)
        activity.switchToTab(MainActivity.TAB_KAONTY)
        activity.switchToTab(MainActivity.TAB_FANDRAISANA)

        // Verify authentic Hymn 1 and Hymn 830 from real hymns.json
        val hymn1 = com.example.data.local.HymnCatalog.getSongByNumber(activity, 1)
        org.junit.Assert.assertEquals("MASINA NY LANITRA", hymn1?.title)

        val hymn830 = com.example.data.local.HymnCatalog.getSongByNumber(activity, 830)
        org.junit.Assert.assertEquals("RAHA MBOLA MAHARITRA NY FANILO MIREHITRA", hymn830?.title)

        // Verify empty state card exists on Fandraisana
        val emptyCard = activity.findViewById<View>(R.id.card_history_empty_state)
        assertNotNull(emptyCard)

        // Test opening HymnReader for several hymns (including multi-verse hymns)
        activity.openHymnReader(1)
        activity.openHymnReader(112)
        activity.openHymnReader(830)
        activity.closeHymnReader()

        // Verify history has consulted hymns and at most 10 items
        val history = com.example.data.local.HymnCatalog.historySongsFlow.value
        org.junit.Assert.assertTrue(history.size in 1..10)
        org.junit.Assert.assertEquals(830, history.first().number)

        // Test opening Hikaroka with search and numpad
        activity.openHikarokaWithSearch("12", openNumpad = true)
        activity.openHikarokaWithSearch("Jesosy", openNumpad = false)

        // Test opening Rakitsary, Fandaharampotoana, Trakta, Fifandraisana
        activity.openRakitsary()
        activity.closeRakitsary()

        activity.openFandaharampotoana()
        activity.closeFandaharampotoana()

        activity.openTrakta()
        activity.closeTrakta()

        activity.openFifandraisana()
        activity.closeFifandraisana()

        // Test Dark Mode toggle and state verification
        activity.toggleThemeMode()
        activity.setThemeMode("nuit")
        org.junit.Assert.assertEquals("nuit", com.example.ui.theme.ThemeManager.themeMode.value)
        org.junit.Assert.assertTrue(com.example.ui.theme.ThemeManager.isDarkMode.value)

        activity.setThemeMode("jour")
        org.junit.Assert.assertEquals("jour", com.example.ui.theme.ThemeManager.themeMode.value)
        org.junit.Assert.assertFalse(com.example.ui.theme.ThemeManager.isDarkMode.value)

        // Verify RecyclerView binding on Hikaroka
        activity.switchToTab(MainActivity.TAB_HIKAROKA)
        val rv = activity.findViewById<RecyclerView>(R.id.rv_hikaroka_results)
        assertNotNull(rv)
        rv.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY)
        )
        rv.layout(0, 0, 1080, 1920)
        assertTrue((rv.adapter?.itemCount ?: 0) > 0)
    }
}
