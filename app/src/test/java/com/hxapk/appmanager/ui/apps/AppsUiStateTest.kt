package com.hxapk.appmanager.ui.apps

import com.hxapk.appmanager.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class AppsUiStateTest {
    private val apps = listOf(
        app("com.example.notes", "Ghi chú", isSystem = false),
        app("com.android.settings", "Cài đặt", isSystem = true),
        app("com.example.disabled", "Notes archive", isSystem = false, isEnabled = false)
    )

    @Test
    fun packageSearchWorksWithoutMatchingTheDisplayName() {
        val state = AppsUiState(apps = apps, query = "EXAMPLE.NOTES")

        assertEquals(listOf("com.example.notes"), state.filteredApps.map { it.packageName })
    }

    @Test
    fun pastedSearchIgnoresSurroundingWhitespace() {
        val state = AppsUiState(apps = apps, query = "  ghi chú  ")

        assertEquals(listOf("com.example.notes"), state.filteredApps.map { it.packageName })
    }

    @Test
    fun systemFilterStillAppliesWhenSearchingByPackage() {
        val state = AppsUiState(apps = apps, filter = AppFilter.SYSTEM, query = "com.")

        assertEquals(listOf("com.android.settings"), state.filteredApps.map { it.packageName })
    }

    @Test
    fun visibleAppCountsDoNotChangeWithTheSearchOrFilter() {
        val state = AppsUiState(apps = apps, filter = AppFilter.USER, query = "notes")

        assertEquals(2, state.userCount)
        assertEquals(1, state.systemCount)
        assertEquals(2, state.filteredApps.size)
    }

    private fun app(
        packageName: String,
        label: String,
        isSystem: Boolean,
        isEnabled: Boolean = true
    ) = AppInfo(
        packageName = packageName,
        label = label,
        versionName = "1.0",
        versionCode = 1,
        isSystem = isSystem,
        isEnabled = isEnabled,
        size = 1_024,
        icon = null
    )
}
