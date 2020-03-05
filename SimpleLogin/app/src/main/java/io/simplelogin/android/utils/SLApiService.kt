package io.simplelogin.android.utils

import android.util.Log
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.utils.enums.SocialService
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private val BASE_URL = when (BuildConfig.BUILD_TYPE == "debug") {
    true -> "https://app.sldev.ovh"
    false -> "https://app.simplelogin.io"
}

private val client = OkHttpClient()

object SLApiService {
    private val CONTENT_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    fun socialLogin(socialService: SocialService, accessToken: String, device: String) {
        val body = """
            {
                "${socialService.serviceName}_token": "$accessToken",
                "device": "$device"
            }
        """.trimIndent()
        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/${socialService.serviceName}")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        Log.d("auth", "${response.code}")
                        Log.d("auth", response.body?.string())
                    }

                    400 -> {
                        // Bad request
                        Log.d("auth", "${response.code}")
                    }

                    else -> {
                        Log.d("auth", "${response.code}")
                    }
                }
            }

        })
    }
}