package com.example.app0202.data.repository

import android.util.Log
import com.example.app0202.Constants
import com.example.app0202.data.api.AuthApi
import com.example.app0202.data.api.RootiCareApi
import com.example.app0202.data.auth.TokenManager
import com.example.app0202.data.local.AppDatabase
import com.example.app0202.data.local.EventTagDbEntity
import com.example.app0202.data.model.*
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

    // ---- API #2: Auth Patient (GET, no body) ----
    suspend fun authPatient(institutionId: String, patientId: String): Result<AuthPatientResponse> {
        return try {
            Log.d(TAG, "authPatient: GET /oauth/vendors/$institutionId/patients/$patientId")
            val response = rootiCareApi.authPatient(institutionId, patientId)
            Log.d(TAG, "authPatient response code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                Log.d(TAG, "authPatient response: vendorName=${body?.vendorName}, " +
                        "subscribedBefore=${body?.subscribedBefore}")

                if (body?.vendorName != null) {
                    // Check subscribedBefore
                    val isSubscribed = when (val sb = body.subscribedBefore) {
                        is Boolean -> sb
                        is String -> sb.equals("true", ignoreCase = true)
                        else -> false
                    }

                    if (isSubscribed) {
                        val msg = "此病患已在其他裝置登入"
                        Log.e(TAG, "authPatient: subscribedBefore=true")
                        Result.failure(Exception(msg))
                    } else {
                        tokenManager.institutionId = institutionId
                        tokenManager.patientId = patientId
                        tokenManager.vendorName = body.vendorName
                        tokenManager.isLoggedIn = true
                        tokenManager.loginTime = System.currentTimeMillis()
                        Log.d(TAG, "authPatient success: vendorName=${body.vendorName}")
                        Result.success(body)
                    }
                } else {
                    val msg = "登入失敗：無效的回應內容 (vendorName 為空)"
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
    // 無論成功或失敗，都清除本地資料
    suspend fun unsubscribePatient(): Result<Unit> {
        val institutionId = tokenManager.institutionId ?: ""
        val patientId = tokenManager.patientId ?: ""

        try {
            if (institutionId.isNotEmpty() && patientId.isNotEmpty()) {
                Log.d(TAG, "=== Unsubscribe Start: institutionId=$institutionId, patientId=$patientId ===")

                // Try 4 combinations of method × path
                val trials = listOf(
                    "A: POST /oauth/vendors/" to suspend { rootiCareApi.unsubscribeVendorPost(institutionId, patientId) },
                    "B: PUT  /oauth/vendors/" to suspend { rootiCareApi.unsubscribeVendorPut(institutionId, patientId) },
                    "C: PUT  /api/v1/institutions/" to suspend { rootiCareApi.unsubscribeInstitutionPut(institutionId, patientId) },
                    "D: POST /api/v1/institutions/" to suspend { rootiCareApi.unsubscribeInstitutionPost(institutionId, patientId) }
                )

                for ((name, call) in trials) {
                    try {
                        val resp = call()
                        val body = try {
                            if (resp.isSuccessful) resp.body()?.string() else resp.errorBody()?.string()
                        } catch (_: Exception) { null }
                        Log.d(TAG, "Unsub $name → code=${resp.code()}, body=${body?.take(200)}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Unsub $name → exception: ${e.message}")
                    }
                }
            } else {
                Log.w(TAG, "Unsubscribe: missing institutionId or patientId, skipping API call")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unsubscribe exception (will still clear local data)", e)
        } finally {
            // 無論成功或失敗，都清除本地資料
            Log.d(TAG, "Clearing local data (always, per API spec)")
            clearLocalData()
        }

        return Result.success(Unit)
    }

    suspend fun clearLocalData() {
        database.eventTagDao().clearAll()
        tokenManager.clearAll()
    }

    // ---- Local DB ----
    suspend fun getLocalEventTags(): List<EventTagDbEntity> {
        return database.eventTagDao().getAll()
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
        return EventTagDbEntity(
            id = tagId,
            tagTime = tagTime,
            tagLocalTime = formatter.format(Date(tagTime)),
            measureMode = measureMode,
            measureRecordId = measureId,
            eventType = symptomTypes?.symptomTypes ?: emptyList(),
            others = symptomTypes?.others,
            exerciseIntensity = exerciseIntensity,
            isRead = true,
            isEdit = false
        )
    }
}
