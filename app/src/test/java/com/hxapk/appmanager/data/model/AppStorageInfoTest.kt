package com.hxapk.appmanager.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStorageInfoTest {
    @Test
    fun cacheIsSeparatedFromDataAndCountedOnceInTotal() {
        val storage = AppStorageInfo.measured(
            apkSize = 10,
            appBytes = 15,
            dataBytes = 100,
            cacheBytes = 20,
            hasUsageAccess = true
        )

        assertEquals(80L, storage.dataSize)
        assertEquals(20L, storage.cacheSize)
        // Installed code includes more than the downloadable APK.
        assertEquals(15L, storage.appSize)
        assertEquals(115L, storage.totalSize)
        assertTrue(storage.hasStorageStats)
    }

    @Test
    fun deniedStatsKeepApkFallbackAndMarkDataUnavailable() {
        val storage = AppStorageInfo(
            apkSize = 10,
            hasUsageAccess = false,
            storageMessage = "Cần quyền Usage Access"
        )

        assertEquals(10L, storage.appSize)
        assertEquals(10L, storage.totalSize)
        assertFalse(storage.hasStorageStats)
        assertFalse(storage.hasUsageAccess)
        assertTrue(storage.isStorageStatsSupported)
        assertEquals("Cần quyền Usage Access", storage.storageMessage)
    }

    @Test
    fun unsupportedDeviceRemainsDistinctFromPermissionDenied() {
        val storage = AppStorageInfo(
            apkSize = 10,
            hasUsageAccess = true,
            isStorageStatsSupported = false,
            storageMessage = "Android 8 trở lên mới hỗ trợ thống kê"
        )

        assertEquals(10L, storage.totalSize)
        assertFalse(storage.hasStorageStats)
        assertTrue(storage.hasUsageAccess)
        assertFalse(storage.isStorageStatsSupported)
    }
}
