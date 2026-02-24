package com.rootilabs.wmeCardiac.data.model

import com.squareup.moshi.Json

data class TotalHistoryResponse(
    val totalRow: Int = 0
)

data class EventTagHistoryResponse(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val totalPage: Int = 0,
    val totalRow: Int = 0,
    val rows: List<VirtualTagEntity> = emptyList()
)

data class VirtualTagEntity(
    val tagId: String? = null,
    val tagTime: Long? = null,
    val exerciseIntensity: Int? = null,
    val symptomTypes: SymptomTypes? = null
)
// Keep SymptomTypes data class for now in case other models still need it via imports, 
// though we'll likely remove it later if unused.
data class SymptomTypes(
    val symptomTypes: List<Int>? = null,
    val others: String? = null
)
