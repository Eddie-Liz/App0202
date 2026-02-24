package com.rootilabs.wmeCardiac.data.model

import com.squareup.moshi.Json

data class AddVirtualTagsRequest(
    val deviceUUID: String,
    val appVersion: String,
    val appType: Int = 1, // 0 = iOS, 1 = Android
    val tags: List<VirtualTagRequest>
)

data class VirtualTagRequest(
    val tagTime: Long,
    val exerciseIntensity: Int,
    val symptomTypes: SymptomTypes
)

data class AddVirtualTagsResponse(
    val institutionId: String? = null,
    val patientId: String? = null,
    val measureRecordId: String? = null,
    val addedSize: Int = 0,
    val updatedSize: Int = 0,
    val failedSize: Int = 0,
    val failedAdding: List<FailedTag>? = null
)

data class FailedTag(
    val tagTime: Long,
    val exerciseIntensity: Int,
    val symptomTypes: List<Int>? = null
)
