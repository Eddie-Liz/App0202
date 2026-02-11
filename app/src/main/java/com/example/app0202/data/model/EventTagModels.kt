package com.example.app0202.data.model

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
    val tagId: String = "",
    val tagTime: Long = 0L,
    val exerciseIntensity: Int = 0,
    val symptomTypes: SymptomTypes? = null
)

data class SymptomTypes(
    val symptomTypes: List<Int> = emptyList(),
    val others: String? = null
)
