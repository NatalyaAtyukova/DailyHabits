package com.habittracker.dailyhabits.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.habittracker.dailyhabits.model.HabitType

class Converters {
    private val gson = Gson()

    // Конвертер для Map<Long, Float>
    @TypeConverter
    fun fromFloatMap(value: Map<Long, Float>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFloatMap(value: String?): Map<Long, Float>? {
        val mapType = object : TypeToken<Map<Long, Float>>() {}.type
        return value?.let { gson.fromJson(it, mapType) }
    }

    // Конвертер для List<String>
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return value?.let { gson.fromJson(it, listType) }
    }
    
    // Конвертер для List<Int>
    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        val listType = object : TypeToken<List<Int>>() {}.type
        return value?.let { gson.fromJson(it, listType) }
    }

    // Конвертер для HabitType
    @TypeConverter
    fun fromHabitType(value: HabitType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toHabitType(value: String?): HabitType? {
        return value?.let { HabitType.valueOf(it) }
    }

    // --- Старый конвертер для Map<Long, Boolean>, который теперь нужно заменить ---
    // @TypeConverter
    // fun fromBooleanMap(value: Map<Long, Boolean>): String {
    //     return gson.toJson(value)
    // }
    //
    // @TypeConverter
    // fun toBooleanMap(value: String): Map<Long, Boolean> {
    //     val mapType = object : TypeToken<Map<Long, Boolean>>() {}.type
    //     return gson.fromJson(value, mapType)
    // }
}