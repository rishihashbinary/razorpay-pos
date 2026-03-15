package com.routehub.pos.helpers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    private const val PREF_NAME = "language_pref"
    private const val KEY_LANGUAGE = "language"

    fun setLocale(context: Context, langCode: String) {

        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()

        updateResources(context, langCode)
    }

    fun loadLocale(context: Context) {

        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val langCode = prefs.getString(KEY_LANGUAGE, "en")

        updateResources(context, langCode!!)
    }

    fun getLanguage(context: Context): String {

        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val langCode = prefs.getString(KEY_LANGUAGE, "en")

        return langCode!!
    }

    private fun updateResources(context: Context, langCode: String) {

        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }
}