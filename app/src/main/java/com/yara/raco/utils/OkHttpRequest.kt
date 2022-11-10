package com.yara.raco.utils

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit

class OkHttpRequest private constructor() {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Throws(
        MalformedURLException::class,
        IllegalArgumentException::class,
        IOException::class,
        IllegalStateException::class
    )
    fun get(
        sUrl: String, accessToken: String? = null, language: String? = null
    ): Response {
        val url = URL(sUrl)

        val requestBuilder = Request.Builder().url(url)

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        if (language != null) {
            requestBuilder.addHeader("Accept-Language", language)
        }

        val request = requestBuilder.build()

        return client.newCall(request).execute()
    }

    @Throws(
        MalformedURLException::class,
        IllegalArgumentException::class,
        IOException::class,
        IllegalStateException::class
    )
    fun post(
        sUrl: String, body: String, mediaType: String? = null, accessToken: String? = null,
    ): Response {
        val formBody = body.toRequestBody(mediaType?.toMediaTypeOrNull())

        val url = URL(sUrl)

        val requestBuilder = Request.Builder()
            .url(url)
            .post(formBody)

        if (accessToken != null) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        val request = requestBuilder.build()

        return client.newCall(request).execute()
    }

    companion object {
        private var instance: OkHttpRequest? = null

        fun getInstance(): OkHttpRequest {
            if (instance == null) instance = OkHttpRequest()
            return instance as OkHttpRequest
        }
    }
}