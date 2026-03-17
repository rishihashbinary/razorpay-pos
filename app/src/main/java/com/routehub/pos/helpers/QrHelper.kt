package com.routehub.pos.helpers

import android.net.Uri

object QrHelper {

    fun extractQrCode(input: String): String? {
        return try {
            val uri = Uri.parse(input)
            uri.getQueryParameter("code") ?: input
        } catch (e: Exception) {
            null
        }
    }
}