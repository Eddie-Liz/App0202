package com.example.app0202.data.api

import com.example.app0202.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RootiCareApi {

    // API #2: Auth Patient (GET, no body)
    @GET("/oauth/vendors/{institutionId}/patients/{patientId}")
    suspend fun authPatient(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String
    ): Response<AuthPatientResponse>

    // API #3: Get Current Measurement Info
    @GET("/api/v1/institutions/{institutionId}/patients/{patientId}/measures/currentMeasurement")
    suspend fun getCurrentMeasurementInfo(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String
    ): Response<MeasurementInfo>

    // API #4: Get Total History Event Tag Count
    @GET("/api/v1/institutions/{institutionId}/patients/{patientId}/measures/{measureId}/virtualTags/totalHistory")
    suspend fun getTotalHistoryCount(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Path("measureId") measureId: String
    ): Response<TotalHistoryResponse>

    // API #5: Get Event Tag History (Paginated)
    @GET("/api/v1/institutions/{institutionId}/patients/{patientId}/measures/{measureId}/virtualTags/history")
    suspend fun getEventTagHistory(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Path("measureId") measureId: String,
        @Query("pageSize") pageSize: Int = 5,
        @Query("pageNumber") pageNumber: Int
    ): Response<EventTagHistoryResponse>

    // === Unsubscribe endpoints (trying multiple paths/methods) ===

    // Path A: POST /oauth/vendors/.../unsubscribe (per API doc)
    @POST("/oauth/vendors/{institutionId}/patients/{patientId}/unsubscribe")
    suspend fun unsubscribeVendorPost(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ResponseBody>

    // Path B: PUT /oauth/vendors/.../unsubscribe
    @PUT("/oauth/vendors/{institutionId}/patients/{patientId}/unsubscribe")
    suspend fun unsubscribeVendorPut(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ResponseBody>

    // Path C: PUT /api/v1/institutions/.../unsubscribe (old working path)
    @PUT("/api/v1/institutions/{institutionId}/patients/{patientId}/unsubscribe")
    suspend fun unsubscribeInstitutionPut(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ResponseBody>

    // Path D: POST /api/v1/institutions/.../unsubscribe
    @POST("/api/v1/institutions/{institutionId}/patients/{patientId}/unsubscribe")
    suspend fun unsubscribeInstitutionPost(
        @Path("institutionId") institutionId: String,
        @Path("patientId") patientId: String,
        @Body body: Map<String, String> = emptyMap()
    ): Response<ResponseBody>
}
