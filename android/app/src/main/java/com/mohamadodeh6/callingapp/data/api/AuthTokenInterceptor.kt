package com.mohamadodeh6.callingapp.data.api

import com.mohamadodeh6.callingapp.utils.SessionStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        sessionStore.token?.let { token ->
            requestBuilder.header("Authorization", "Bearer ".plus(token))
        }
        return chain.proceed(requestBuilder.build())
    }
}
