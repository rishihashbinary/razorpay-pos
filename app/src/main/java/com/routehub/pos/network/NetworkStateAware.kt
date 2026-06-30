package com.routehub.pos.network

interface NetworkStateAware {
    fun onNetworkStateChanged(state: NetworkState)
}