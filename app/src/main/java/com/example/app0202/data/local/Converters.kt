package com.example.app0202.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? =
        value?.joinToString(",")

    @TypeConverter
    fun toIntList(value: String?): List<Int>? =
        value?.takeIf { it.isNotEmpty() }?.split(",")?.mapNotNull { it.toIntOrNull() }
}
