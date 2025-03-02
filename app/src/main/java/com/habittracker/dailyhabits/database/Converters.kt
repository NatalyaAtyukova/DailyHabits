package com.habittracker.dailyhabits.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .create()

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