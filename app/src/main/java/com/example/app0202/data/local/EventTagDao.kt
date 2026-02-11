package com.example.app0202.data.local

import androidx.room.*

@Dao
interface EventTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<EventTagDbEntity>)

    @Query("SELECT * FROM event_tags WHERE measure_mode = :measureMode ORDER BY tag_time DESC")
    suspend fun getAllByMeasureMode(measureMode: Int): List<EventTagDbEntity>

    @Query("SELECT * FROM event_tags ORDER BY tag_time DESC")
    suspend fun getAll(): List<EventTagDbEntity>

    @Query("SELECT COUNT(*) FROM event_tags")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM event_tags WHERE is_edit")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM event_tags")
    suspend fun clearAll()
}
