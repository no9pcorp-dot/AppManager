package com.hxapk.appmanager.data.model

data class AppStorageInfo(
    val apkSize: Long,
    val appSize: Long = apkSize,
    val dataSize: Long = 0,
    val cacheSize: Long = 0,
    val hasStorageStats: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val isStorageStatsSupported: Boolean = true,
    val storageMessage: String? = null
) {
    val totalSize: Long get() = appSize + dataSize + cacheSize

    companion object {
        fun measured(
            apkSize: Long,
            appBytes: Long,
            dataBytes: Long,
            cacheBytes: Long,
            hasUsageAccess: Boolean
        ): AppStorageInfo {
            val allData = dataBytes.coerceAtLeast(0)
            // StorageStats.dataBytes includes cache; the chart needs disjoint parts.
            val cache = cacheBytes.coerceIn(0, allData)
            return AppStorageInfo(
                apkSize = apkSize,
                appSize = appBytes.coerceAtLeast(0),
                dataSize = allData - cache,
                cacheSize = cache,
                hasStorageStats = true,
                hasUsageAccess = hasUsageAccess
            )
        }
    }
}