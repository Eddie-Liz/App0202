package com.rootilabs.wmeCardiac.data.repository

import android.util.Log
import com.rootilabs.wmeCardiac.Constants
import com.rootilabs.wmeCardiac.data.api.AuthApi
import com.rootilabs.wmeCardiac.data.api.RootiCareApi
import com.rootilabs.wmeCardiac.data.auth.TokenManager
import com.rootilabs.wmeCardiac.data.local.AppDatabase
import com.rootilabs.wmeCardiac.data.local.EventTagDbEntity
import com.rootilabs.wmeCardiac.data.model.*
import retrofit2.Response
import com.squareup.moshi.Moshi
import java.text.SimpleDateFormat
import java.util.*

class RootiCareRepository(
    private val authApi: AuthApi,
    private val rootiCareApi: RootiCareApi,
    private val tokenManager: TokenManager,
    private val database: AppDatabase,
    private val moshi: Moshi
) {
    companion object {
        private const val TAG = "RootiCareRepo"
    }

    // ---- API #1: Token ----
    suspend fun getToken(): Result<String> {
        return try {
            val response = authApi.getToken(
                basicAuth = Constants.BASIC_AUTH,
                body = mapOf("grant_type" to "client_credentials")
            )
            if (response.isSuccessful) {
                val token = response.body()?.accessToken
                if (token != null) {
                    tokenManager.accessToken = token
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token is null"))
                }
            } else {
                Result.failure(Exception("Token request failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getToken error", e)
            Result.failure(e)
        }
    }

    // ---- API #2: Auth Patient (GET, subscribes the device) ----
    suspend fun authPatient(institutionId: String, patientId: String): Result<AuthPatientResponse> {
        return try {
            Log.d(TAG, "authPatient (Standard): institutionId=$institutionId, patientId=$patientId")
            
            val response = rootiCareApi.authPatient(institutionId, patientId)
            Log.d(TAG, "authPatient response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "authPatient response: vendorName=${body?.vendorName}, subscribedBefore=${body?.subscribedBefore}")
                
                // Robust check for subscribedBefore
                val isSubscribed = when (val sb = body?.subscribedBefore) {
                    is Boolean -> sb
                    is String -> sb.equals("true", ignoreCase = true)
                    else -> false
                }
                
                if (isSubscribed) {
                    val msg = "此病患已在其他裝置登入"
                    Log.e(TAG, "authPatient: subscribedBefore=true")
                    return Result.failure(Exception(msg))
                }

                if (body?.vendorName != null) {
                    tokenManager.institutionId = institutionId
                    tokenManager.patientId = patientId
                    tokenManager.vendorName = body.vendorName
                    tokenManager.isLoggedIn = true
                    tokenManager.loginTime = System.currentTimeMillis()
                    Log.d(TAG, "authPatient success: vendorName=${body.vendorName}")
                    Result.success(body)
                } else {
                    val msg = "登入失敗：無效的回應內容"
                    Log.e(TAG, msg)
                    Result.failure(Exception(msg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "authPatient failed: HTTP ${response.code()} - $errorBody")
                val errorMsg = parseError(errorBody)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "authPatient exception", e)
            Result.failure(e)
        }
    }

    // ---- API #3: Current Measurement ----
    suspend fun getCurrentMeasurement(
        institutionId: String,
        patientId: String
    ): Result<MeasurementInfo> {
        return try {
            val response = rootiCareApi.getCurrentMeasurementInfo(institutionId, patientId)
            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "getCurrentMeasurement success: $body")
                body?.let {
                    tokenManager.measureRecordId = it.measureRecordId
                    tokenManager.serverDeviceId = it.deviceId
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("Get measurement failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentMeasurement error", e)
            Result.failure(e)
        }
    }

    // ---- API #4: Total History Count ----
    suspend fun getTotalHistoryCount(
        institutionId: String,
        patientId: String,
        measureId: String
    ): Result<Int> {
        return try {
            val response = rootiCareApi.getTotalHistoryCount(institutionId, patientId, measureId)
            if (response.isSuccessful) {
                Result.success(response.body()?.totalRow ?: 0)
            } else {
                Result.failure(Exception("Get history count failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTotalHistoryCount error", e)
            Result.failure(e)
        }
    }

    // ---- API #5: Fetch All Event Tag History (paginated) ----
    suspend fun fetchAllEventTagHistory(
        institutionId: String,
        patientId: String,
        measureId: String
    ): Result<Int> {
        return try {
            var currentPage = 1
            var totalSaved = 0

            while (true) {
                val response = rootiCareApi.getEventTagHistory(
                    institutionId = institutionId,
                    patientId = patientId,
                    measureId = measureId,
                    pageSize = Constants.PAGE_SIZE,
                    pageNumber = currentPage
                )
                val body = response.body() ?: break

                val dbEntities = body.rows.mapNotNull { tag ->
                    try {
                        tag.toDbEntity(measureId, MeasurementInfo.MODE_VIRTUAL_TAG)
                    } catch (e: Exception) {
                        Log.e(TAG, "parseVirtualEventTag error: ${e.message}")
                        null
                    }
                }
                database.eventTagDao().insertAll(dbEntities)
                totalSaved += dbEntities.size

                if (currentPage >= body.totalPage) break
                currentPage++
            }

            Result.success(totalSaved)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllEventTagHistory error", e)
            Result.failure(e)
        }
    }

    // ---- API #6: Unsubscribe (Logout) ----
    suspend fun unsubscribePatient(
        explicitInstitutionId: String? = null,
        explicitPatientId: String? = null
    ): Result<Unit> {
        val institutionId = explicitInstitutionId ?: tokenManager.institutionId ?: ""
        val patientId = explicitPatientId ?: tokenManager.patientId ?: ""

        if (institutionId.isEmpty() || patientId.isEmpty()) {
            Log.w(TAG, "Unsubscribe: missing institutionId or patientId, clearing anyway")
            clearLocalData()
            return Result.success(Unit)
        }

        return try {
            // Strategy C (The confirmed winner): POST base path
            Log.d(TAG, "Attempting Logout (Primary Strategy) - POST base path: inst=$institutionId, patient=$patientId")
            val response = rootiCareApi.unsubscribePatient(institutionId, patientId)
            
            Log.d(TAG, "Logout result code: ${response.code()}")
            
            if (response.isSuccessful || response.code() == 204) {
                Log.d(TAG, "Logout success, clearing local data.")
            } else {
                // Secondary Strategy: POST with /unsubscribe suffix
                Log.d(TAG, "Primary failed (${response.code()}), trying fallback Strategy A (POST .../unsubscribe)")
                val pushToken = tokenManager.pushToken
                val request = UnsubscribeRequest(deviceToken = pushToken)
                val fallbackResponse = rootiCareApi.unsubscribePatientWithSuffix(institutionId, patientId, request)
                Log.d(TAG, "Fallback Strategy result: ${fallbackResponse.code()}")
            }

            // Always clear local data on user's logout intent
            clearLocalData()
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Logout exception, clearing local anyway", e)
            clearLocalData()
            Result.success(Unit)
        }
    }

    // ---- API #7: Upload Virtual Event Tags ----
    suspend fun uploadVirtualEventTags(tags: List<EventTagDbEntity>): Result<AddVirtualTagsResponse> {
        val institutionId = tokenManager.institutionId ?: ""
        val patientId = tokenManager.patientId ?: ""
        val measureId = tokenManager.measureRecordId ?: ""

        if (institutionId.isEmpty() || patientId.isEmpty() || measureId.isEmpty()) {
            return Result.failure(Exception("Missing required IDs for upload"))
        }

        return try {
            val request = AddVirtualTagsRequest(
                deviceUUID = tokenManager.deviceId,
                appVersion = tokenManager.appVersion,
                appType = 1, // Android
                tags = tags.map { it.toVirtualTagRequest() }
            )

            val result = rootiCareApi.addVirtualEventTags(
                institutionId = institutionId,
                patientId = patientId,
                measureId = measureId,
                body = request
            )

            if (result.isSuccessful) {
                val response = result.body()!!
                // Mark as uploaded in local DB
                val updatedTags = tags.map { it.copy(isEdit = false) }
                database.eventTagDao().insertAll(updatedTags)
                Result.success(response)
            } else {
                val errorBody = result.errorBody()?.string()
                val apiError = parseError(errorBody)
                Log.e(TAG, "Upload failed: $apiError")
                Result.failure(Exception(apiError))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }

    suspend fun clearLocalData() {
        database.eventTagDao().clearAll()
        tokenManager.clearAll()
    }

    // ---- Local DB ----
    suspend fun getLocalEventTags(): List<EventTagDbEntity> {
        return database.eventTagDao().getAll()
    }

    suspend fun getUnsyncedCount(): Int {
        return database.eventTagDao().getUnsyncedCount()
    }

    suspend fun saveEventTag(entity: EventTagDbEntity) {
        database.eventTagDao().insertAll(listOf(entity))
    }

    // ---- Helpers ----
    private fun parseError(errorJson: String?): String {
        if (errorJson == null) return "未知錯誤"
        return try {
            val adapter = moshi.adapter(ApiError::class.java)
            val error = adapter.fromJson(errorJson)
            when (error?.error) {
                "patient_already_subscribed" -> "此病患已在其他裝置登入"
                "invalid_patient" -> "無效的病患 ID"
                "invalid_institution_id" -> "無效的機構 ID"
                else -> error?.errorDescription ?: "未知錯誤"
            }
        } catch (e: Exception) {
            "未知錯誤"
        }
    }

    private fun VirtualTagEntity.toDbEntity(measureId: String, measureMode: Int): EventTagDbEntity {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val tt = tagTime ?: 0L
        val ei = exerciseIntensity ?: 0
        val symptoms = symptomTypes?.symptomTypes ?: emptyList()

        return EventTagDbEntity(
            id = tagId ?: "TAG-${System.currentTimeMillis()}",
            tagTime = tt,
            tagLocalTime = if (tt > 0L) formatter.format(Date(tt)) else "Unknown",
            measureMode = measureMode,
            measureRecordId = measureId,
            eventType = symptoms,
            others = symptomTypes?.others,
            exerciseIntensity = ei,
            isRead = true,
            isEdit = false
        )
    }

    private fun EventTagDbEntity.toVirtualTagRequest(): VirtualTagRequest {
        return VirtualTagRequest(
            tagTime = tagTime,
            exerciseIntensity = exerciseIntensity,
            symptomTypes = SymptomTypes(
                symptomTypes = eventType,
                others = others
            )
        )
    }
}
