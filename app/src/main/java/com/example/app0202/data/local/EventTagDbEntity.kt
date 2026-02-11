package com.example.app0202.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_tags")
data class EventTagDbEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "tag_time") val tagTime: Long,
    @ColumnInfo(name = "tag_local_time") val tagLocalTime: String,
    @ColumnInfo(name = "measure_mode") val measureMode: Int,
    @ColumnInfo(name = "measure_record_id") val measureRecordId: String,
    @ColumnInfo(name = "event_type") val eventType: List<Int>,
    @ColumnInfo(name = "others") val others: String?,
    @ColumnInfo(name = "exercise_intensity") val exerciseIntensity: Int,
    @ColumnInfo(name = "is_read") val isRead: Boolean = true,
    @ColumnInfo(name = "is_edit") val isEdit: Boolean = false
)
