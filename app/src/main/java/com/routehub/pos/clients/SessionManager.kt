package com.routehub.pos.clients

object SessionManager {

    private var token: String? = null

    fun setToken(value: String){
        token = value
    }

    fun getToken(): String{
        return token ?: ""
    }
}