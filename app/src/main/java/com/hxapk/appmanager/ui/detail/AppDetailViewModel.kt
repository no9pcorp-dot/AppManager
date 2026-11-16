package com.hxapk.appmanager.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hxapk.appmanager.data.repository.AppRepository
import com.hxapk.appmanager.data.util.UsageAccessHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
) : ViewModel() {
    private val packageName = savedStateHandle.get<String>("packageName").orEmpty()
    private val _uiState = MutableStateFlow(
        AppDetailUiState(
            packageName = packageName,
            hasUsageAccess = hasUsageAccess(),
            isStorageStatsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        )
    )
    val uiState = _uiState.asStateFlow()
    private val loadMutex = Mutex()
    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                _uiState.update {
                    it.copy(isLoading = true, error = null, hasUsageAccess = hasUsageAccess())
                }
                try {
                    val app = repository.getAppDetail(packageName)
                    val storage = app.storage
                    _uiState.value = AppDetailUiState(
                        isLoading = false,
                        appName = app.appName,
                        packageName = app.packageName,
                        versionName = app.versionName,
                        versionCode = app.versionCode,
                        isSystem = app.isSystem,
                        isEnabled = app.isEnabled,
                        isLaunchable = app.isLaunchable,
                        icon = app.icon,
                        apkSize = storage.apkSize,
                        appSize = storage.appSize,
                        dataSize = storage.dataSize,
                        cacheSize = storage.cacheSize,
                        hasStorageStats = storage.hasStorageStats,
                        hasUsageAccess = storage.hasUsageAccess,
                        isStorageStatsSupported = storage.isStorageStatsSupported,
                        storageMessage = storage.storageMessage,
                        minSdk = app.minSdk,
                        targetSdk = app.targetSdk,
                        apkPath = app.apkPath
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không tải được thông tin ứng dụng. Ứng dụng có thể đã bị gỡ hoặc không còn truy cập được."
                        )
                    }
                }
            }
        }
    }

    fun hasUsageAccess(): Boolean = UsageAccessHelper.hasUsageAccess(context)

    fun requestUsageAccess() {
        if (!UsageAccessHelper.openUsageAccessSettings(context)) {
            _uiState.update { it.copy(error = "Không mở được trang cấp quyền trên thiết bị này.") }
        }
    }

    fun launchApp() {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) {
                _uiState.update { it.copy(error = "Ứng dụng này không có màn hình để mở.") }
                return
            }
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            _uiState.update { it.copy(error = "Không mở được ứng dụng. Hãy tải lại thông tin rồi thử lại.") }
        }
    }

    fun openSystemAppInfo() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "Không mở được Cài đặt trên thiết bị này.") }
            }
        }
    }

    fun backupApk() { /* TODO: APK export has not been implemented. */ }
    fun shareApk() { /* TODO: APK sharing has not been implemented. */ }
}