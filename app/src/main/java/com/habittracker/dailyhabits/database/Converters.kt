package com.habittracker.dailyhabits.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromDailyStatusMap(map: Map<Long, Boolean>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toDailyStatusMap(data: String): Map<Long, Boolean> {
        val type = object : TypeToken<Map<Long, Boolean>>() {}.type
        return gson.fromJson(data, type)
    }
}