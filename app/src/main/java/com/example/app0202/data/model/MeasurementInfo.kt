package com.example.app0202.data.model

data class MeasurementInfo(
    val measureRecordOid: Long = 0L,
    val measureRecordId: String = "",
    val mode: Int = 0,
    val state: Int = 0,
    val launchTime: Long = 0L,
    val expectedEndTime: Long = 0L,
    val deviceId: String = ""
) {
    fun isMeasuring(): Boolean {
        val now = System.currentTimeMillis()
        // If expectedEndTime is small (e.g. 10 digits), treat as seconds and convert to millis
        val endTimeMillis = if (expectedEndTime < 100000000000L) expectedEndTime * 1000 else expectedEndTime
        
        return state == STATE_MEASURING
            && expectedEndTime != 0L
            && now < endTimeMillis
    }

    fun isVirtualTagMode(): Boolean = mode == MODE_VIRTUAL_TAG

    companion object {
        const val STATE_MEASURING = 0
        const val MODE_VIRTUAL_TAG = 0  // Holter
        const val MODE_MONITOR = 1
        const val MODE_MCT_TAG = 2
    }
}
