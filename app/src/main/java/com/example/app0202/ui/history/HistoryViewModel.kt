package com.example.app0202.ui.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app0202.data.local.EventTagDbEntity
import com.example.app0202.di.ServiceLocator
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val repository = ServiceLocator.repository

    var tags by mutableStateOf<List<EventTagDbEntity>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            tags = repository.getLocalEventTags()
        }
    }
}
