package com.routehub.pos.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getReadableDate(dateStr: String?): String {
        return try {
            if (dateStr.isNullOrEmpty()) return "NA"

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

            val date = inputFormat.parse(dateStr)
            outputFormat.format(date!!)

        } catch (e: Exception) {
            e.printStackTrace()
            "NA"
        }
    }
}