package com.rootilabs.wmeCardiac.ui.login

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootilabs.wmeCardiac.di.ServiceLocator
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
        viewModelScope.launch { performLogin() }
    }

    private suspend fun performLogin() {
        try {
            uiState = uiState.copy(isLoading = true, error = null, statusMessage = "取得 Token 中...")
            Log.d(TAG, "=== Login Start === institutionId=$institutionId, patientId=$patientId")

            // Step 1: Get Token
            val tokenResult = repository.getToken()
            Log.d(TAG, "Step 1 getToken: success=${tokenResult.isSuccess}")
            if (tokenResult.isFailure) {
                val msg = "Token 取得失敗: ${tokenResult.exceptionOrNull()?.message}"
                Log.e(TAG, msg)
                uiState = uiState.copy(isLoading = false, error = msg)
                return
            }

            // Step 2: Auth Patient
            uiState = uiState.copy(statusMessage = "登入驗證中...")
            val authResult = repository.authPatient(institutionId, patientId)
            Log.d(TAG, "Step 2 authPatient: success=${authResult.isSuccess}")
            if (authResult.isFailure) {
                val msg = "登入失敗: ${authResult.exceptionOrNull()?.message}"
                Log.e(TAG, msg)
                uiState = uiState.copy(isLoading = false, error = msg)
                return
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
                return
            }

            // Validation 1: isMeasuring?
            val isMeasuring = measurementInfo.isMeasuring()
            val now = System.currentTimeMillis()
            Log.d(TAG, "Measurement Check: state=${measurementInfo.state}, expectedEndTime=${measurementInfo.expectedEndTime}, now=$now")
            
            ServiceLocator.tokenManager.isMeasuring = isMeasuring
            measurementInfo.deviceId?.let {
                ServiceLocator.tokenManager.deviceId = it
                Log.d(TAG, "Server deviceId saved: $it")
            }
            
            if (!isMeasuring) {
                val msg = "登入失敗：您現在並沒有在錄製中 (state=${measurementInfo.state})"
                Log.w(TAG, msg)
                // Block login if not measuring
                repository.unsubscribePatient()
                uiState = uiState.copy(isLoading = false, error = "您現在並沒有在錄製中，無法登入！")
                return
            }

            // Validation 2: isVirtualTagMode?
            if (!measurementInfo.isVirtualTagMode()) {
                val msg = "登入失敗：不支援此模式 (mode=${measurementInfo.mode})"
                Log.w(TAG, msg)
                repository.unsubscribePatient()
                uiState = uiState.copy(isLoading = false, error = "此模式不支援 Android (僅限 VirtualTag)")
                return
            }

            // Step 4: Check Total History Count (API #4) & Fetch History (API #5)
            uiState = uiState.copy(statusMessage = "同步事件標註中...")
            val measureId = ServiceLocator.tokenManager.measureRecordId
            Log.d(TAG, "Step 4 measureId=$measureId")
            if (measureId != null) {
                val countResult = repository.getTotalHistoryCount(institutionId, patientId, measureId)
                val totalRow = countResult.getOrNull() ?: 0
                Log.d(TAG, "Step 4a totalHistoryCount: totalRow=$totalRow")

                if (totalRow > 0) {
                    uiState = uiState.copy(statusMessage = "下載 $totalRow 筆歷史標註...")
                    val fetchResult = repository.fetchAllEventTagHistory(institutionId, patientId, measureId)
                    Log.d(TAG, "Step 4b fetchHistory: saved=${fetchResult.getOrNull()} records")
                }
            }

            Log.d(TAG, "=== Login Success ===")
            uiState = uiState.copy(isLoading = false, loginSuccess = true)
        } catch (e: Throwable) {
            Log.e(TAG, "FATAL CRASH during login flow", e)
            uiState = uiState.copy(isLoading = false, error = "發生嚴重錯誤: ${e.message}")
        }
    }

    fun forceLogin() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null, statusMessage = "正在強制解除其他裝置綁定...")
                Log.d(TAG, "=== Force Login Start === institutionId=$institutionId, patientId=$patientId")
                
                // Step 0: Force Unsubscribe
                repository.unsubscribePatient(institutionId, patientId)
                
                // Add a small delay to allow server state to sync
                kotlinx.coroutines.delay(1000)
                
                // Step 1: Proceed with normal login
                performLogin()
            } catch (e: Throwable) {
                Log.e(TAG, "Force login failed", e)
                uiState = uiState.copy(isLoading = false, error = "強制登入失敗: ${e.message}")
            }
        }
    }
}
