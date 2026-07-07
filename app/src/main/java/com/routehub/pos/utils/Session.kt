package com.routehub.pos.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple project-wide session store backed by SharedPreferences (plain text).
 *
 * Call [init] once in Application.onCreate() before any other use.
 *
 * Generic use:
 *     Session.store("some-key", "some-value")
 *     val v = Session.get("some-key")            // null if absent
 *     val v = Session.get("some-key", "default") // default if absent
 *
 * Typed helpers for auth:
 *     Session.setAuthToken(token)   // also flips the logged-in flag on
 *     Session.getAuthToken()
 *     Session.isLoggedIn()
 *     Session.clear()               // logout / 401
 */
object Session {

    private const val PREF_NAME = "routehub_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_LOGGED_IN = "is_logged_in"

    private var prefs: SharedPreferences? = null

    /** Call once, early (Application.onCreate). Safe to call more than once. */
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun prefs(): SharedPreferences =
        prefs ?: throw IllegalStateException(
            "Session not initialized. Call Session.init(context) in Application.onCreate()."
        )

    // ---------------------------------------------------------------------
    // Generic key/value API (reusable across the whole project)
    // ---------------------------------------------------------------------

    fun store(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
    }

    fun store(key: String, value: Boolean) {
        prefs().edit().putBoolean(key, value).apply()
    }

    fun store(key: String, value: Int) {
        prefs().edit().putInt(key, value).apply()
    }

    fun store(key: String, value: Long) {
        prefs().edit().putLong(key, value).apply()
    }

    fun get(key: String, defaultValue: String? = null): String? =
        prefs().getString(key, defaultValue)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs().getBoolean(key, defaultValue)

    fun getInt(key: String, defaultValue: Int): Int =
        prefs().getInt(key, defaultValue)

    fun getLong(key: String, defaultValue: Long): Long =
        prefs().getLong(key, defaultValue)

    fun contains(key: String): Boolean = prefs().contains(key)

    fun remove(key: String) {
        prefs().edit().remove(key).apply()
    }

    // ---------------------------------------------------------------------
    // Auth convenience helpers (built on the generic API above)
    // ---------------------------------------------------------------------

    /** Stores the token and marks the user as logged in. Call on successful login. */
    fun setAuthToken(token: String) {
        prefs().edit()
            .putString(KEY_TOKEN, token)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    /** @return the stored auth token, or null if none. */
    fun getAuthToken(): String? = get(KEY_TOKEN)

    /** @return true only if a session was persisted and not cleared. */
    fun isLoggedIn(): Boolean = getBoolean(KEY_LOGGED_IN, false) && getAuthToken() != null

    /** Wipes the whole session. Call on explicit logout and on a 401. */
    fun clear() {
        prefs().edit().clear().apply()
    }
}
