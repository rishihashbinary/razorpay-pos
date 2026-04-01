package com.routehub.pos

interface PrintCallback {
    fun onSuccess()
    fun onError(error: String?)
}