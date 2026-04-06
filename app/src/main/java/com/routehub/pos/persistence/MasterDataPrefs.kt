package com.routehub.pos.persistence

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.routehub.pos.models.PropertyCategory
import com.routehub.pos.models.PropertyType
import com.routehub.pos.models.PropertyUsageType
import com.routehub.pos.models.Ward
import com.routehub.pos.models.Zone

object MasterDataPrefs {

    private const val PREF_NAME = "master_data"
    private const val KEY_ZONES = "zones"
    private const val KEY_WARDS = "wards"
    private const val KEY_TYPES = "types"
    private const val KEY_CATEGORIES = "categories"
    private const val KEY_USAGE = "usage"

    private val gson = Gson()

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ---------------- SAVE ----------------

    fun saveZones(context: Context, data: List<Zone>) {
        prefs(context).edit().putString(KEY_ZONES, gson.toJson(data)).apply()
    }

    fun saveWards(context: Context, data: List<Ward>) {
        prefs(context).edit().putString(KEY_WARDS, gson.toJson(data)).apply()
    }

    fun saveTypes(context: Context, data: List<PropertyType>) {
        prefs(context).edit().putString(KEY_TYPES, gson.toJson(data)).apply()
    }

    fun saveCategories(context: Context, data: List<PropertyCategory>) {
        prefs(context).edit().putString(KEY_CATEGORIES, gson.toJson(data)).apply()
    }

    fun saveUsage(context: Context, data: List<PropertyUsageType>) {
        prefs(context).edit().putString(KEY_USAGE, gson.toJson(data)).apply()
    }

    // ---------------- GET ----------------

    fun getZones(context: Context): List<Zone> {
        val json = prefs(context).getString(KEY_ZONES, null) ?: return emptyList()
        val type = object : TypeToken<List<Zone>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getWards(context: Context): List<Ward> {
        val json = prefs(context).getString(KEY_WARDS, null) ?: return emptyList()
        val type = object : TypeToken<List<Ward>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getTypes(context: Context): List<PropertyType> {
        val json = prefs(context).getString(KEY_TYPES, null) ?: return emptyList()
        val type = object : TypeToken<List<PropertyType>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getCategories(context: Context): List<PropertyCategory> {
        val json = prefs(context).getString(KEY_CATEGORIES, null) ?: return emptyList()
        val type = object : TypeToken<List<PropertyCategory>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getUsage(context: Context): List<PropertyUsageType> {
        val json = prefs(context).getString(KEY_USAGE, null) ?: return emptyList()
        val type = object : TypeToken<List<PropertyUsageType>>() {}.type
        return gson.fromJson(json, type)
    }

    fun isDataAvailable(context: Context): Boolean {
        return prefs(context).contains(KEY_ZONES)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}