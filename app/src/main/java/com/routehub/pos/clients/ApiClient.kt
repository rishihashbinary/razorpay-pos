package com.routehub.pos.clients

import android.content.Intent
import com.routehub.pos.RouteHubApp
import com.routehub.pos.screens.LoginActivity
import com.routehub.pos.utils.Session
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

//        private const val BASE_URL = "https://app.ecosense-enviro.com/api/"
    private const val BASE_URL = "https://dev.ecosense-enviro.com/api/"

    // The login call itself can return a 401 for a plain wrong-password attempt —
    // that's not an expired session, so it must not trigger the global logout/redirect.
    private const val LOGIN_PATH = "users/login"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->

        val token = SessionManager.getToken() // your token storage

        val request: Request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("projectid", "68e4bdf3c73509002fc447de")
            .build()

        chain.proceed(request)
    }

    private val sessionExpiryInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        val isLoginCall = request.url.encodedPath.contains(LOGIN_PATH)

        if (response.code == 401 && !isLoginCall) {
            Session.clear()

            val intent = Intent(RouteHubApp.appContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            RouteHubApp.appContext.startActivity(intent)
        }

        response
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(sessionExpiryInterceptor)
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}