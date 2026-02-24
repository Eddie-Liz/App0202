package com.rootilabs.wmeCardiac.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rootilabs.wmeCardiac.data.local.EventTagDbEntity
import com.rootilabs.wmeCardiac.di.ServiceLocator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.rootilabs.wmeCardiac.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Icon Source Wrapper
sealed class IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource()
    data class Resource(val resId: Int) : IconSource()
    data class Text(val text: String) : IconSource()
}

// 症狀類型
data class SymptomItem(
    val id: Int,
    val name: String,
    val icon: IconSource
)

val SYMPTOM_LIST = listOf(
    SymptomItem(1, "胸悶、胸部疼痛", IconSource.Resource(R.drawable.ic_symptom_chest_pain)), 
    SymptomItem(2, "暈眩", IconSource.Resource(R.drawable.ic_symptom_dizziness)),
    SymptomItem(3, "心悸", IconSource.Resource(R.drawable.ic_symptom_palpitations)),
    SymptomItem(4, "疲倦虛弱", IconSource.Resource(R.drawable.ic_symptom_fatigue)),
    SymptomItem(5, "心跳過快", IconSource.Resource(R.drawable.ic_symptom_rapid_heartbeat)),
    SymptomItem(6, "呼吸急促", IconSource.Resource(R.drawable.ic_symptom_shortness_of_breath))
)

// 運動強度
data class ExerciseItem(
    val id: Int,
    val name: String,
    val description: String,
    val icon: IconSource
)

val EXERCISE_LIST = listOf(
    ExerciseItem(3, "高強度運動", "跑步、騎自行車、有氧舞蹈等", IconSource.Resource(R.drawable.ic_exercise_high_intensity)),
    ExerciseItem(2, "中強度運動", "上下樓梯、正常行走等", IconSource.Resource(R.drawable.ic_exercise_medium_intensity)),
    ExerciseItem(1, "輕度運動", "散步", IconSource.Resource(R.drawable.ic_exercise_low_intensity)),
    ExerciseItem(0, "靜態活動", "坐著、睡覺等", IconSource.Resource(R.drawable.ic_exercise_resting))
)

enum class TagFlowStep {
    IDLE, SYMPTOM_SELECTION, EXERCISE_SELECTION, CONFIRMATION
}

data class MainUiState(
    val isLoading: Boolean = false,
    val isMeasuring: Boolean = true,
    val eventTags: List<EventTagDbEntity> = emptyList(),
    val lastTagTime: String? = null,
    val logoutSuccess: Boolean = false,
    val error: String? = null,
    val tagFlowStep: TagFlowStep = TagFlowStep.IDLE,
    val tagTime: Long = 0L,
    val tagTimeFormatted: String = "",
    val selectedSymptoms: List<Int> = emptyList(),
    val otherSymptom: String = "",
    val selectedExercise: Int = -1,
    val hasUnsyncedTags: Boolean = false,
    val showSyncErrorBadge: Boolean = false,
    val loginTimeDisplay: String = ""
)

class MainViewModel : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val repository = ServiceLocator.repository
    private val tokenManager = ServiceLocator.tokenManager

    var uiState by mutableStateOf(MainUiState())
        private set

    private var isSyncing = false

    init {
        loadEventTags()
    }

    fun loadEventTags() {
        viewModelScope.launch {
            try {
                val tags = repository.getLocalEventTags()
                val unsyncedCount = tags.count { it.isEdit }
                val lastTime = tags.firstOrNull()?.tagLocalTime

                val loginTime = tokenManager.loginTime
                val isMeasuring = tokenManager.isMeasuring
                val loginTimeStr = if (loginTime > 0) {
                    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(loginTime))
                } else {
                    "未知"
                }

                uiState = uiState.copy(
                    eventTags = tags,
                    hasUnsyncedTags = unsyncedCount > 0,
                    showSyncErrorBadge = uiState.showSyncErrorBadge && unsyncedCount > 0,
                    lastTagTime = lastTime,
                    loginTimeDisplay = loginTimeStr,
                    isMeasuring = isMeasuring
                )

                if (unsyncedCount > 0) {
                    triggerAutoSync()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error loading event tags", e)
                uiState = uiState.copy(error = "載入資料庫錯誤: ${e.message}")
            }
        }
    }

    private fun triggerAutoSync() {
        if (isSyncing) return
        
        viewModelScope.launch {
            try {
                val unsynced = uiState.eventTags.filter { it.isEdit }
                if (unsynced.isEmpty()) return@launch

                isSyncing = true
                Log.d(TAG, "Auto-sync: triggering upload for ${unsynced.size} tags")
                val result = repository.uploadVirtualEventTags(unsynced)
                if (result.isSuccess) {
                    uiState = uiState.copy(showSyncErrorBadge = false)
                    loadEventTags()
                } else {
                    val error = result.exceptionOrNull()
                    if (error != null && isNetworkError(error)) {
                        uiState = uiState.copy(showSyncErrorBadge = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto-sync failed: ${e.message}")
                if (isNetworkError(e)) {
                    uiState = uiState.copy(showSyncErrorBadge = true)
                }
            } finally {
                isSyncing = false
            }
        }
    }

    private fun isNetworkError(t: Throwable): Boolean {
        return t is java.net.UnknownHostException || 
               t is java.net.ConnectException || 
               t is java.net.SocketTimeoutException ||
               t.message?.contains("Unable to resolve host", ignoreCase = true) == true
    }

    private var isCheckingStatus = false

    fun checkRecordingStatus() {
        if (isCheckingStatus) return
        
        viewModelScope.launch {
            try {
                isCheckingStatus = true
                val institutionId = tokenManager.institutionId ?: ""
                val patientId = tokenManager.patientId ?: ""
                
                if (institutionId.isNotEmpty() && patientId.isNotEmpty()) {
                    val result = repository.getCurrentMeasurement(institutionId, patientId)
                    val info = result.getOrNull()
                    if (info != null) {
                        val serverStatus = info.isMeasuring()
                        if (uiState.isMeasuring != serverStatus) {
                            uiState = uiState.copy(isMeasuring = serverStatus)
                            tokenManager.isMeasuring = serverStatus
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkRecordingStatus failed: ${e.message}")
            } finally {
                isCheckingStatus = false
            }
        }
    }

    // ---- Tag Flow ----
    fun onTagPressed() {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        uiState = uiState.copy(
            tagFlowStep = TagFlowStep.SYMPTOM_SELECTION,
            tagTime = now,
            tagTimeFormatted = formatter.format(Date(now)),
            selectedSymptoms = emptyList(),
            otherSymptom = "",
            selectedExercise = -1
        )
    }

    fun toggleSymptom(symptomId: Int) {
        val current = uiState.selectedSymptoms.toMutableList()
        if (current.contains(symptomId)) current.remove(symptomId)
        else current.add(symptomId)
        uiState = uiState.copy(selectedSymptoms = current)
    }

    fun setOtherSymptom(text: String) {
        uiState = uiState.copy(otherSymptom = text)
    }

    fun goToExerciseSelection() {
        uiState = uiState.copy(tagFlowStep = TagFlowStep.EXERCISE_SELECTION)
    }

    fun selectExercise(exerciseId: Int) {
        uiState = uiState.copy(selectedExercise = exerciseId)
    }

    fun goToConfirmation() {
        uiState = uiState.copy(tagFlowStep = TagFlowStep.CONFIRMATION)
    }

    fun confirmTag() {
        viewModelScope.launch {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val entity = EventTagDbEntity(
                id = "TAG-${System.currentTimeMillis()}",
                tagTime = uiState.tagTime,
                tagLocalTime = formatter.format(Date(uiState.tagTime)),
                measureMode = 0,
                measureRecordId = tokenManager.measureRecordId ?: "",
                eventType = uiState.selectedSymptoms,
                others = uiState.otherSymptom.takeIf { it.isNotBlank() },
                exerciseIntensity = uiState.selectedExercise,
                isRead = false,
                isEdit = true
            )
            
            repository.saveEventTag(entity)
            
            uiState = uiState.copy(
                tagFlowStep = TagFlowStep.IDLE,
                lastTagTime = entity.tagLocalTime
            )
            loadEventTags()

            // Try to upload immediately
            Log.d(TAG, "Attempting auto-upload for new tag: ${entity.id}")
            val uploadResult = repository.uploadVirtualEventTags(listOf(entity))
            if (uploadResult.isSuccess) {
                Log.i(TAG, "Auto-upload success")
            } else {
                Log.e(TAG, "Auto-upload failed: ${uploadResult.exceptionOrNull()?.message}")
            }
        }
    }

    fun cancelTagFlow() {
        uiState = uiState.copy(tagFlowStep = TagFlowStep.IDLE)
    }

    fun goBackToSymptoms() {
        uiState = uiState.copy(tagFlowStep = TagFlowStep.SYMPTOM_SELECTION)
    }

    fun goBackToExercise() {
        uiState = uiState.copy(tagFlowStep = TagFlowStep.EXERCISE_SELECTION)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true)
                Log.d(TAG, "=== Logout Start ===")

                // Refresh token before unsubscribe
                try {
                    val tokenResult = ServiceLocator.repository.getToken()
                    Log.d(TAG, "Token refresh: success=${tokenResult.isSuccess}")
                } catch (e: Exception) {
                    Log.e(TAG, "Token refresh failed: ${e.message}")
                }

                val result = repository.unsubscribePatient()
                if (result.isSuccess) {
                    Log.d(TAG, "=== Logout Complete (local data cleared) ===")
                    uiState = uiState.copy(isLoading = false, logoutSuccess = true)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "原因不明"
                    Log.e(TAG, "=== Logout Failed: $errorMsg ===")
                    uiState = uiState.copy(isLoading = false, error = "登出失敗：$errorMsg")
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Logout error", e)
                uiState = uiState.copy(isLoading = false, error = "登出發生錯誤: ${e.message}")
            }
        }
    }
}
