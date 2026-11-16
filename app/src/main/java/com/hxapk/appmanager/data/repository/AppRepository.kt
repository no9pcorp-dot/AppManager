package com.hxapk.appmanager.data.repository

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.storage.StorageManager
import com.hxapk.appmanager.data.model.AppDetailInfo
import com.hxapk.appmanager.data.model.AppInfo
import com.hxapk.appmanager.data.model.AppStorageInfo
import com.hxapk.appmanager.data.util.UsageAccessHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pm: PackageManager = context.packageManager

    @Suppress("DEPRECATION")
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        pm.getInstalledPackages(0).mapNotNull { pkg ->
            currentCoroutineContext().ensureActive()
            val ai = pkg.applicationInfo ?: return@mapNotNull null
            AppInfo(
                packageName = pkg.packageName,
                label = appLabel(ai, pkg.packageName),
                versionName = pkg.versionName ?: "—",
                versionCode = versionCode(pkg),
                isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                isEnabled = ai.enabled,
                size = apkSize(ai),
                icon = appIcon(ai)
            )
        }.sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    @Suppress("DEPRECATION")
    suspend fun getAppDetail(packageName: String): AppDetailInfo = withContext(Dispatchers.IO) {
        require(packageName.isNotBlank()) { "Thiếu tên gói ứng dụng." }
        val pkg = pm.getPackageInfo(packageName, 0)
        val ai = pkg.applicationInfo
            ?: throw PackageManager.NameNotFoundException(packageName)
        currentCoroutineContext().ensureActive()
        val size = apkSize(ai)
        val usageAccess = UsageAccessHelper.hasUsageAccess(context)
        val storage = storageInfo(ai, packageName, size, usageAccess)
        currentCoroutineContext().ensureActive()
        AppDetailInfo(
            appName = appLabel(ai, packageName),
            packageName = packageName,
            versionName = pkg.versionName ?: "—",
            versionCode = versionCode(pkg),
            isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
            isEnabled = ai.enabled,
            isLaunchable = pm.getLaunchIntentForPackage(packageName) != null,
            icon = appIcon(ai),
            minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ai.minSdkVersion else 0,
            targetSdk = ai.targetSdkVersion,
            apkPath = ai.sourceDir.orEmpty(),
            storage = storage
        )
    }

    @Suppress("DEPRECATION")
    private fun versionCode(pkg: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pkg.longVersionCode
        else pkg.versionCode.toLong()

    private fun appLabel(ai: ApplicationInfo, packageName: String): String =
        try {
            ai.loadLabel(pm).toString()
        } catch (_: Exception) {
            packageName
        }

    private fun appIcon(ai: ApplicationInfo) =
        try {
            ai.loadIcon(pm)
        } catch (_: Exception) {
            null
        }

    private fun apkSize(ai: ApplicationInfo): Long =
        (listOfNotNull(ai.sourceDir) + ai.splitSourceDirs.orEmpty())
            .distinct()
            .sumOf { path ->
                try {
                    File(path).length()
                } catch (_: SecurityException) {
                    0L
                }
            }

    private fun storageInfo(
        ai: ApplicationInfo,
        packageName: String,
        apkSize: Long,
        usageAccess: Boolean
    ): AppStorageInfo {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = usageAccess,
                isStorageStatsSupported = false,
                storageMessage = "Android 8 trở lên mới hỗ trợ thống kê dung lượng. Hiện chỉ có dung lượng APK."
            )
        }
        if (!usageAccess && packageName != context.packageName) {
            return AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = false,
                storageMessage = "Cấp quyền Usage Access để xem dung lượng ứng dụng, dữ liệu và cache."
            )
        }

        return try {
            val manager = context.getSystemService(StorageStatsManager::class.java)
                ?: return AppStorageInfo(
                    apkSize = apkSize,
                    hasUsageAccess = usageAccess,
                    storageMessage = "Thiết bị không cung cấp dịch vụ thống kê dung lượng."
                )
            val stats = manager.queryStatsForPackage(
                ai.storageUuid ?: StorageManager.UUID_DEFAULT,
                packageName,
                Process.myUserHandle()
            )
            AppStorageInfo.measured(
                apkSize = apkSize,
                appBytes = stats.appBytes,
                dataBytes = stats.dataBytes,
                cacheBytes = stats.cacheBytes,
                hasUsageAccess = usageAccess
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: SecurityException) {
            AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = UsageAccessHelper.hasUsageAccess(context),
                storageMessage = "Android từ chối đọc thống kê dung lượng. Kiểm tra quyền Usage Access rồi thử lại."
            )
        } catch (_: PackageManager.NameNotFoundException) {
            AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = usageAccess,
                storageMessage = "Ứng dụng không còn được cài đặt. Hãy tải lại danh sách."
            )
        } catch (_: IOException) {
            AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = usageAccess,
                storageMessage = "Không đọc được bộ nhớ chứa ứng dụng. Hãy thử lại."
            )
        } catch (_: Exception) {
            AppStorageInfo(
                apkSize = apkSize,
                hasUsageAccess = usageAccess,
                storageMessage = "Không đọc được thống kê dung lượng trên thiết bị này. Hiện chỉ có dung lượng APK."
            )
        }
    }
}