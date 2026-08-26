package com.routehub.pos.clients

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.routehub.pos.models.EmployeeDetail
import com.routehub.pos.models.Property
import com.routehub.pos.models.responses.ApiObjectResponse
import com.routehub.pos.services.EmployeeService
import com.routehub.pos.utils.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ScopeEntry(val id: String, val label: String)

object EmployeeScope {

    private const val TAG = "EmployeeScope"
    private const val KEY_WARDS = "employee_scope_wards"
    private const val KEY_ZONES = "employee_scope_zones"

    private val gson = Gson()
    private val listType = object : TypeToken<List<ScopeEntry>>() {}.type

    fun refresh(employeeService: EmployeeService, employeeId: String) {
        employeeService.getEmployeeDetail(employeeId).enqueue(object : Callback<ApiObjectResponse<EmployeeDetail>> {
            override fun onResponse(
                call: Call<ApiObjectResponse<EmployeeDetail>>,
                response: Response<ApiObjectResponse<EmployeeDetail>>
            ) {
                if (!response.isSuccessful) {
                    Log.w(TAG, "Employee detail fetch failed: HTTP ${response.code()} - failing open")
                    return
                }

                val detail = response.body()?.data
                val wards = detail?.wardIds.orEmpty().map { ScopeEntry(it._id, it.number ?: it._id) }
                val zones = detail?.zoneIds.orEmpty().map { ScopeEntry(it._id, it.name ?: it._id) }

                store(KEY_WARDS, wards)
                store(KEY_ZONES, zones)

                Log.d(TAG, "Scope resolved: ${wards.size} ward(s), ${zones.size} zone(s)")
            }

            override fun onFailure(call: Call<ApiObjectResponse<EmployeeDetail>>, t: Throwable) {
                Log.w(TAG, "Employee detail fetch error: ${t.message} - failing open")
            }
        })
    }

    fun isInScope(property: Property?): Boolean {
        val wards = getWards()
        val zones = getZones()

        // Nothing persisted yet (not fetched, or fetch failed) - fail open.
        if (wards.isEmpty() && zones.isEmpty()) return true

        val propertyWardId = property?.wardId?._id
        val propertyZoneId = property?.zoneId?._id ?: property?.wardId?.zoneId?._id

        if (wards.isNotEmpty()) {
            return propertyWardId != null && wards.any { it.id == propertyWardId }
        }

        return propertyZoneId != null && zones.any { it.id == propertyZoneId }
    }

    fun getAssignedLabel(): String {
        val wards = getWards()
        if (wards.isNotEmpty()) return wards.joinToString(", ") { it.label }

        val zones = getZones()
        if (zones.isNotEmpty()) return zones.joinToString(", ") { it.label }

        return ""
    }

    private fun getWards(): List<ScopeEntry> = readList(KEY_WARDS)
    private fun getZones(): List<ScopeEntry> = readList(KEY_ZONES)

    private fun store(key: String, entries: List<ScopeEntry>) {
        Session.store(key, gson.toJson(entries))
    }

    private fun readList(key: String): List<ScopeEntry> {
        val json = Session.get(key) ?: return emptyList()
        return try {
            gson.fromJson<List<ScopeEntry>>(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}