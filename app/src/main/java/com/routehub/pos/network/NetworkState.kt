package com.routehub.pos.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper

enum class NetworkState { STABLE, UNSTABLE, OFFLINE }

/**
 * Tracks real connectivity (via ConnectivityManager) plus "unstable" state
 * derived from recent API call outcomes (reported externally via
 * recordSuccess/recordFailure), so a device that *shows* connected but
 * whose calls are failing/timing out still gets flagged.
 */
class NetworkMonitor(
    private val context: Context,
    private val onStateChanged: (NetworkState) -> Unit
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val mainHandler = Handler(Looper.getMainLooper())

    private var hasActiveNetwork = false
    private var currentState = NetworkState.OFFLINE

    // rolling window of recent API call outcomes
    private val recentResults = ArrayDeque<Boolean>()
    private val windowSize = 3
    private val unstableThreshold = 2 // 2 of last 3 failed -> unstable

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            hasActiveNetwork = true
            recentResults.clear() // give it a clean slate to prove itself
            evaluateState()
        }

        override fun onLost(network: Network) {
            hasActiveNetwork = false
            evaluateState()
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            hasActiveNetwork = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            evaluateState()
        }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun stop() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // callback already unregistered
        }
    }

    /** Call this after every real API call (transaction sync, payment call, etc.) */
    fun recordSuccess() = recordResult(true)

    fun recordFailureOrTimeout() = recordResult(false)

    private fun recordResult(success: Boolean) {
        recentResults.addLast(success)
        if (recentResults.size > windowSize) recentResults.removeFirst()
        evaluateState()
    }

    private fun evaluateState() {
        val newState = when {
            !hasActiveNetwork -> NetworkState.OFFLINE
            recentFailureCount() >= unstableThreshold -> NetworkState.UNSTABLE
            else -> NetworkState.STABLE
        }

        if (newState != currentState) {
            currentState = newState
            mainHandler.post { onStateChanged(newState) }
        }
    }

    private fun recentFailureCount(): Int =
        recentResults.count { !it }
}