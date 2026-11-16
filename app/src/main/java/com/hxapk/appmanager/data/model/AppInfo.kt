package com.hxapk.appmanager.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val size: Long,
    val icon: Drawable?
)
