package com.hxapk.appmanager.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hxapk.appmanager.data.util.toReadableSize
import com.hxapk.appmanager.ui.components.ActionIconButton
import com.hxapk.appmanager.ui.components.InfoCard
import com.hxapk.appmanager.ui.components.RefreshOnResume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    onBack: () -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    RefreshOnResume(viewModel::refresh)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.appName.ifBlank { "Chi tiết ứng dụng" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)

        when {
            state.isLoading -> {
                Box(contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(contentModifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "Không thể tải thông tin ứng dụng",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::refresh) { Text("Thử lại") }
                    }
                }
            }
            else -> {
                LazyColumn(state = listState, modifier = contentModifier) {
                    if (state.isStorageStatsSupported && !state.hasUsageAccess && !state.hasStorageStats) {
                        item(key = "usage_access") {
                            UsageAccessCard(onRequest = viewModel::requestUsageAccess)
                        }
                    }
                    item(key = "header") {
                        AppHeader(
                            state = state,
                            onLaunch = viewModel::launchApp,
                            onSystemInfo = viewModel::openSystemAppInfo
                        )
                    }
                    item(key = "general") {
                        InfoCard("Thông tin chung", Icons.Outlined.Info) {
                            InfoRow("Phiên bản", state.versionName)
                            InfoRow("Mã build", state.versionCode.toString())
                            InfoRow("Min SDK", "API ${state.minSdk}")
                            InfoRow("Target SDK", "API ${state.targetSdk}")
                            InfoRow("Đường dẫn APK", state.apkPath)
                        }
                    }
                    item(key = "storage") { StorageCard(state) }
                    item(key = "footer") { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun UsageAccessCard(onRequest: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Cần quyền truy cập mức sử dụng",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Cấp quyền để xem dung lượng dữ liệu và cache của ứng dụng.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRequest, modifier = Modifier.align(Alignment.End)) {
                Text("Cấp quyền")
            }
        }
    }
}

@Composable
private fun AppHeader(
    state: AppDetailUiState,
    onLaunch: () -> Unit,
    onSystemInfo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val drawable = state.icon
        if (drawable != null) {
            val bitmap = remember(drawable) { drawable.toBitmap(192, 192).asImageBitmap() }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                Modifier.size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text("?") }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            state.appName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            state.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text("v${state.versionName} (${state.versionCode})")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppBadge(if (state.isSystem) "Hệ thống" else "Người dùng")
            AppBadge(
                if (state.hasStorageStats) state.totalSize.toReadableSize()
                else "Đã biết ${state.totalSize.toReadableSize()}"
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ActionIconButton(
                icon = Icons.Outlined.PlayArrow,
                label = "Mở",
                onClick = onLaunch,
                modifier = Modifier.weight(1f),
                enabled = state.isEnabled && state.isLaunchable
            )
            ActionIconButton(
                icon = Icons.Outlined.Info,
                label = "Chi tiết",
                onClick = onSystemInfo,
                modifier = Modifier.weight(1f)
            )
            ActionIconButton(
                icon = Icons.Outlined.SaveAlt,
                label = "Sao lưu",
                onClick = {},
                modifier = Modifier.weight(1f),
                enabled = false
            )
            ActionIconButton(
                icon = Icons.Outlined.Share,
                label = "Chia sẻ",
                onClick = {},
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }
        Text(
            "Sao lưu và chia sẻ đang phát triển",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun StorageCard(state: AppDetailUiState) {
    val appColor = MaterialTheme.colorScheme.primary
    val dataColor = MaterialTheme.colorScheme.secondary
    val cacheColor = MaterialTheme.colorScheme.tertiary

    InfoCard("Dung lượng", Icons.Outlined.Storage) {
        if (state.totalSize > 0L) {
            Canvas(
                Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
            ) {
                val total = state.totalSize.toFloat()
                val appWidth = (state.appSize / total).coerceIn(0f, 1f) * size.width
                val dataWidth = (state.dataSize / total).coerceIn(0f, 1f) * size.width
                val cacheWidth = (state.cacheSize / total).coerceIn(0f, 1f) * size.width
                drawRect(appColor, size = Size(appWidth, size.height))
                drawRect(
                    dataColor,
                    topLeft = Offset(appWidth, 0f),
                    size = Size(dataWidth, size.height)
                )
                drawRect(
                    cacheColor,
                    topLeft = Offset(appWidth + dataWidth, 0f),
                    size = Size(cacheWidth, size.height)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        StorageRow(appColor, "Ứng dụng", state.appSize)
        StorageRow(
            dataColor, "Dữ liệu (không gồm cache)", state.dataSize,
            showUnavailable = !state.hasStorageStats
        )
        StorageRow(
            cacheColor, "Cache", state.cacheSize,
            showUnavailable = !state.hasStorageStats
        )
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        InfoRow(
            if (state.hasStorageStats) "Tổng cộng" else "Tổng đã biết",
            state.totalSize.toReadableSize(),
            bold = true
        )
        if (!state.hasStorageStats) {
            Text(
                "Chưa bao gồm dữ liệu và cache.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        InfoRow("Tệp APK (gồm split)", state.apkSize.toReadableSize())
        Text(
            "Dung lượng tệp APK nằm trong phần ứng dụng.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.storageMessage?.takeIf { it.isNotBlank() }?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StorageRow(
    color: Color,
    label: String,
    size: Long,
    showUnavailable: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Text(
            if (showUnavailable) "Không khả dụng" else size.toReadableSize(),
            modifier = Modifier.weight(0.65f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.End
        )
    }
}

