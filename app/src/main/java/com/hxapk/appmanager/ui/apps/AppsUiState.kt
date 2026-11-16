package com.hxapk.appmanager.ui.apps

import com.hxapk.appmanager.data.model.AppInfo

enum class AppFilter { ALL, USER, SYSTEM }

data class AppsUiState(
    val isLoading: Boolean = true,
    val apps: List<AppInfo> = emptyList(),
    val filter: AppFilter = AppFilter.ALL,
    val query: String = "",
    val error: String? = null
) {
    val filteredApps: List<AppInfo> by lazy {
        val search = query.trim()
        apps.filter { app ->
            val matchesFilter = when (filter) {
                AppFilter.ALL -> true
                AppFilter.USER -> !app.isSystem
                AppFilter.SYSTEM -> app.isSystem
            }
            matchesFilter && (
                search.isEmpty() ||
                    app.label.contains(search, ignoreCase = true) ||
                    app.packageName.contains(search, ignoreCase = true)
                )
        }
    }

    val userCount: Int by lazy { apps.count { !it.isSystem } }
    val systemCount: Int by lazy { apps.count { it.isSystem } }
}