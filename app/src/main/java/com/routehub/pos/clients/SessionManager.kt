package com.routehub.pos.clients

import com.routehub.pos.utils.Session

object SessionManager {

    private const val KEY_USER_ID = "user_id"

    private var token: String? = null
    private var userId: String? = null

    fun setToken(value: String?){
        token = value
        if (value != null) {
            Session.setAuthToken(value)
        }
    }

    fun getToken(): String{
        return token
            ?: Session.getAuthToken()
            ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYwNTQzMDVlYTk2NjU4NDVlMTZlMjJiNyIsImVtcGxveWVlSWQiOiI2NTI1OWQ3YmIwMGY2NTg4ZmU0YzRlY2YiLCJpYXQiOjE3NzI5Njc4NzgsImV4cCI6MzU0NTkzOTM1Nn0.sDUv9zNEreF4KM4EweilKxYpBXtRwncW8NWBJYiwNxM"
    }

    fun getUserId(): String? {
        return userId ?: Session.get(KEY_USER_ID)
    }

    fun setUserId(value: String?){
        userId = value
        if (value != null) {
            Session.store(KEY_USER_ID, value)
        }
    }
}