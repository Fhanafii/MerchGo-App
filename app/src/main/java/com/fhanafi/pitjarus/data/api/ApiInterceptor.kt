package com.fhanafi.pitjarus.data.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // TODO: Attach request headers when authentication is implemented.
        return chain.proceed(chain.request())
    }
}
