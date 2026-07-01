package com.routehub.pos.onboarding

import android.content.Context

object OnboardingManager {
    private const val PREF_NAME = "onboarding"

    fun hasSeen(context: Context, key: String): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(key, false)

    fun markSeen(context: Context, key: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(key, true).apply()
    }
}