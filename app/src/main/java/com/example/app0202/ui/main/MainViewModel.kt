package com.example.app0202.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app0202.data.local.EventTagDbEntity
import com.example.app0202.di.ServiceLocator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 症狀類型
data class SymptomItem(
    val id: Int,
    val name: String,
    val icon: String
)

val SYMPTOM_LIST = listOf(
    SymptomItem(1, "胸悶、胸痛", "🫀"),
    SymptomItem(2, "頭暈", "😵"),
    SymptomItem(3, "心悸", "💓"),
    SymptomItem(4, "疲倦虛弱", "😫"),
    SymptomItem(5, "心跳過快", "💗"),
    SymptomItem(6, "呼吸急促", "😤")
)

// 運動強度
data class ExerciseItem(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String
)

val EXERCISE_LIST = listOf(
    ExerciseItem(3, "高強度運動", "跑步、騎自行車、有氧舞蹈等", "🏃"),
    ExerciseItem(2, "中強度運動", "上下樓梯、正常行走等", "🚶"),
    ExerciseItem(1, "輕度運動", "散步", "🧘"),
    ExerciseItem(0, "靜態活動", "坐著、睡覺等", "🧘‍♂️")
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
    val tagSaved: Boolean = false,
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

    init {
        loadEventTags()
    }

    fun loadEventTags() {
        viewModelScope.launch {
            val tags = repository.getLocalEventTags()
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
                lastTagTime = lastTime,
                loginTimeDisplay = loginTimeStr,
                isMeasuring = isMeasuring
            )
        }
    }

    // ---- Tag Flow ----
    fun onTagPressed() {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.getDefault())
        uiState = uiState.copy(
            tagFlowStep = TagFlowStep.SYMPTOM_SELECTION,
            tagTime = now,
            tagTimeFormatted = formatter.format(Date(now)),
            selectedSymptoms = emptyList(),
            otherSymptom = "",
            selectedExercise = -1,
            tagSaved = false
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
            val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
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
                isEdit = false
            )
            repository.saveEventTag(entity)
            uiState = uiState.copy(
                tagFlowStep = TagFlowStep.IDLE,
                tagSaved = true,
                lastTagTime = entity.tagLocalTime
            )
            loadEventTags()
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
            uiState = uiState.copy(isLoading = true)
            Log.d(TAG, "=== Logout Start ===")
            Log.d(TAG, "institutionId=${tokenManager.institutionId}, patientId=${tokenManager.patientId}")

            // Refresh token before unsubscribe (in case it expired)
            try {
                val tokenResult = ServiceLocator.repository.getToken()
                Log.d(TAG, "Token refresh: success=${tokenResult.isSuccess}")
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh failed: ${e.message}")
            }

            // Unsubscribe - always clears local data per API spec
            repository.unsubscribePatient()
            Log.d(TAG, "=== Logout Complete (local data cleared) ===")
            uiState = uiState.copy(isLoading = false, logoutSuccess = true)
        }
    }
}
