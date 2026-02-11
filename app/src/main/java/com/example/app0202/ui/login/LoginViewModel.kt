package com.example.app0202.ui.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app0202.di.ServiceLocator
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val statusMessage: String = "",
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val repository = ServiceLocator.repository

    var uiState by mutableStateOf(LoginUiState())
        private set

    var institutionId by mutableStateOf("")
    var patientId by mutableStateOf("")

    fun login() {
        if (institutionId.isBlank() || patientId.isBlank()) {
            uiState = uiState.copy(error = "請填寫 Account ID 和 ID Number")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, statusMessage = "取得 Token 中...")
            Log.d(TAG, "=== Login Start === institutionId=$institutionId, patientId=$patientId")

            // Step 1: Get Token
            val tokenResult = repository.getToken()
            Log.d(TAG, "Step 1 getToken: success=${tokenResult.isSuccess}")
            if (tokenResult.isFailure) {
                val msg = "Token 取得失敗: ${tokenResult.exceptionOrNull()?.message}"
                Log.e(TAG, msg)
                uiState = uiState.copy(isLoading = false, error = msg)
                return@launch
            }

            // Step 2: Auth Patient
            uiState = uiState.copy(statusMessage = "登入驗證中...")
            val authResult = repository.authPatient(institutionId, patientId)
            Log.d(TAG, "Step 2 authPatient: success=${authResult.isSuccess}")
            if (authResult.isFailure) {
                val msg = "登入失敗: ${authResult.exceptionOrNull()?.message}"
                Log.e(TAG, msg)
                uiState = uiState.copy(isLoading = false, error = msg)
                return@launch
            }

            // Step 3: Get Current Measurement
            uiState = uiState.copy(statusMessage = "取得量測資訊中...")
            val measureResult = repository.getCurrentMeasurement(institutionId, patientId)
            Log.d(TAG, "Step 3 getCurrentMeasurement: success=${measureResult.isSuccess}")
            
            val measurementInfo = measureResult.getOrNull()
            if (measureResult.isFailure || measurementInfo == null) {
                val msg = "量測資訊取得失敗: ${measureResult.exceptionOrNull()?.message}"
                Log.e(TAG, msg)
                uiState = uiState.copy(isLoading = false, error = msg)
                return@launch
            }

            // Validation 1: isMeasuring?
            val isMeasuring = measurementInfo.isMeasuring()
            val now = System.currentTimeMillis()
            Log.d(TAG, "Measurement Check: state=${measurementInfo.state}, expectedEndTime=${measurementInfo.expectedEndTime}, now=$now")
            Log.d(TAG, "Measurement Check: isMeasuring=$isMeasuring (state==0? ${measurementInfo.state == 0}, timeValid? ${measurementInfo.expectedEndTime != 0L && now < measurementInfo.expectedEndTime})")
            
            ServiceLocator.tokenManager.isMeasuring = isMeasuring
            
            if (!isMeasuring) {
                Log.w(TAG, "User is not measuring (state=${measurementInfo.state}), continuing login to show status.")
            }

            // Validation 2: isVirtualTagMode?
            if (!measurementInfo.isVirtualTagMode()) {
                val msg = "登入失敗：不支援此模式 (mode=${measurementInfo.mode})"
                Log.w(TAG, msg)
                repository.unsubscribePatient() // Always clears local data per API spec
                Log.d(TAG, "Rejection: unsubscribed and cleared local data")
                
                uiState = uiState.copy(isLoading = false, error = "此模式不支援 Android (僅限 VirtualTag)")
                return@launch
            }

            // Step 4: Check Total History Count (API #4) & Fetch History (API #5)
            uiState = uiState.copy(statusMessage = "同步事件標註中...")
            val measureId = ServiceLocator.tokenManager.measureRecordId
            Log.d(TAG, "Step 4 measureId=$measureId")
            if (measureId != null) {
                // Step 4a: Check total count first
                val countResult = repository.getTotalHistoryCount(institutionId, patientId, measureId)
                val totalRow = countResult.getOrNull() ?: 0
                Log.d(TAG, "Step 4a totalHistoryCount: totalRow=$totalRow")

                // Step 4b: Only fetch if there are records
                if (totalRow > 0) {
                    uiState = uiState.copy(statusMessage = "下載 $totalRow 筆歷史標註...")
                    val fetchResult = repository.fetchAllEventTagHistory(institutionId, patientId, measureId)
                    Log.d(TAG, "Step 4b fetchHistory: saved=${fetchResult.getOrNull()} records")
                } else {
                    Log.d(TAG, "Step 4b: No history records, skipping download")
                }
            } else {
                Log.w(TAG, "Warning: measureId is null, skipping history fetch")
            }

            Log.d(TAG, "=== Login Success ===")
            uiState = uiState.copy(isLoading = false, loginSuccess = true)
        }
    }
}
