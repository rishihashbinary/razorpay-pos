package com.routehub.pos.clients

object SessionManager {

    private var token: String? = null
    private var userId: String? = null

    fun setToken(value: String?){
        token = value
    }

    fun getToken(): String{
        return token ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYwNTQzMDVlYTk2NjU4NDVlMTZlMjJiNyIsImVtcGxveWVlSWQiOiI2NTI1OWQ3YmIwMGY2NTg4ZmU0YzRlY2YiLCJpYXQiOjE3NzI5Njc4NzgsImV4cCI6MzU0NTkzOTM1Nn0.sDUv9zNEreF4KM4EweilKxYpBXtRwncW8NWBJYiwNxM"
    }

    fun getUserId(): String? {
        return userId;
    }

    fun setUserId(value: String?){
        userId = value
    }
}