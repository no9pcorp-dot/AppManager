package com.hxapk.appmanager.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hxapk.appmanager.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class AppsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState = _uiState.asStateFlow()
    private val loadMutex = Mutex()
    private var loadJob: Job? = null

    init {
        loadApps()
    }

    fun loadApps() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadMutex.withLock {
                _uiState.update { it.copy(isLoading = true, error = null) }
                try {
                    val apps = repository.getInstalledApps()
                    _uiState.update { it.copy(isLoading = false, apps = apps) }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Không tải được danh sách ứng dụng. Hãy thử lại."
                        )
                    }
                }
            }
        }
    }

    fun setFilter(filter: AppFilter) = _uiState.update { it.copy(filter = filter) }
    fun setQuery(query: String) = _uiState.update { it.copy(query = query) }
}