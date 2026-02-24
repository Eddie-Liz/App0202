package com.rootilabs.wmeCardiac.data.model

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

data class SymptomTypes(
    val symptomTypes: List<Int>? = null,
    val others: String? = null
)
