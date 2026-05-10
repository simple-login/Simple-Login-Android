package io.simplelogin.core.network

import io.simplelogin.core.model.Constants
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

interface BaseUrlProvider {
    fun getBaseUrl(): String
    fun updateBaseUrl(url: String)
}

object BaseUrlProviderImpl : BaseUrlProvider {
    private var _baseUrl: String = Constants.DEFAULT_BASE_URL

    override fun getBaseUrl() = _baseUrl

    override fun updateBaseUrl(url: String) {
        _baseUrl = url
    }
}

class DynamicBaseUrlInterceptor(val baseUrlProvider: BaseUrlProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newBaseUrlString = baseUrlProvider.getBaseUrl()
        val newBaseUrl = newBaseUrlString.toHttpUrlOrNull()

        if (newBaseUrl == null) {
            assert(false) { "Invalid base url $newBaseUrlString" }
            return chain.proceed(originalRequest)
        }

        val newUrl = originalRequest.url.newBuilder()
            .scheme(newBaseUrl.scheme)
            .host(newBaseUrl.host)
            .port(newBaseUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }
}
