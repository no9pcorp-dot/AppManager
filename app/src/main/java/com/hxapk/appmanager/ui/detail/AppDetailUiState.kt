package com.hxapk.appmanager.ui.detail

import android.graphics.drawable.Drawable

data class AppDetailUiState(
    val isLoading: Boolean = true,
    val appName: String = "",
    val packageName: String = "",
    val versionName: String = "",
    val versionCode: Long = 0,
    val isSystem: Boolean = false,
    val isEnabled: Boolean = true,
    val isLaunchable: Boolean = false,
    val icon: Drawable? = null,
    val apkSize: Long = 0,
    val appSize: Long = 0,
    val dataSize: Long = 0,
    val cacheSize: Long = 0,
    val hasStorageStats: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val isStorageStatsSupported: Boolean = true,
    val storageMessage: String? = null,
    val minSdk: Int = 0,
    val targetSdk: Int = 0,
    val apkPath: String = "",
    val error: String? = null
) {
    val totalSize: Long get() = appSize + dataSize + cacheSize
}