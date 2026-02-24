package com.rootilabs.wmeCardiac.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootilabs.wmeCardiac.data.local.EventTagDbEntity
import com.rootilabs.wmeCardiac.di.ServiceLocator
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val repository = ServiceLocator.repository

    var tags by mutableStateOf<List<EventTagDbEntity>>(emptyList())
        private set

    var isSyncing by mutableStateOf(false)
        private set

    init {
        loadTags()
    }

    fun loadTags() {
        viewModelScope.launch {
            tags = repository.getLocalEventTags()
        }
    }

    fun retryUnsyncedTags() {
        viewModelScope.launch {
            val unsynced = tags.filter { it.isEdit }
            if (unsynced.isEmpty()) return@launch

            try {
                isSyncing = true
                val result = repository.uploadVirtualEventTags(unsynced)
                if (result.isSuccess) {
                    loadTags() // Refresh UI
                }
            } catch (e: Exception) {
                // Error handled by repository logging
            } finally {
                isSyncing = false
            }
        }
    }
}
