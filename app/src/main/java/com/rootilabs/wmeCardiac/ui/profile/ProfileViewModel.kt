package com.rootilabs.wmeCardiac.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootilabs.wmeCardiac.di.ServiceLocator
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val logoutSuccess: Boolean = false,
    val unsyncedCount: Int = 0,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    private val repository = ServiceLocator.repository

    init {
        checkUnsyncedData()
    }

    fun checkUnsyncedData() {
        viewModelScope.launch {
            val count = repository.getUnsyncedCount()
            uiState = uiState.copy(unsyncedCount = count)
        }
    }

    fun logout() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                // Perform full logout (including unsubscribe)
                val result = repository.unsubscribePatient()
                if (result.isSuccess) {
                    uiState = uiState.copy(isLoading = false, logoutSuccess = true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "原因不明"
                    uiState = uiState.copy(isLoading = false, error = "登出失敗：$errorMsg")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "錯誤: ${e.message}")
            }
        }
    }
}
