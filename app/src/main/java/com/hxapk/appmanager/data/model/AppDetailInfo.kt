package com.hxapk.appmanager.data.model

import android.graphics.drawable.Drawable

data class AppDetailInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystem: Boolean,
    val isEnabled: Boolean,
    val isLaunchable: Boolean,
    val icon: Drawable?,
    val minSdk: Int,
    val targetSdk: Int,
    val apkPath: String,
    val storage: AppStorageInfo
)