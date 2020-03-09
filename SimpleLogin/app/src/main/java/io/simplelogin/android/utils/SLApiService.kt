package io.simplelogin.android.utils

import com.google.gson.Gson
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.enums.SocialService
import io.simplelogin.android.utils.model.UserLogin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

private val BASE_URL = when (BuildConfig.BUILD_TYPE == "debug") {
    true -> "https://app.sldev.ovh"
    false -> "https://app.simplelogin.io"
}

private val client = OkHttpClient()

object SLApiService {
    private val CONTENT_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    fun login(email: String, password: String, device: String, completion: (userLogin: UserLogin?, error: SLError?) -> Unit) {
        var body = """
            {
                "email": "$email",
                "password": "$password",
                "device": "$device"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$BASE_URL/api/auth/login")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val userLogin = Gson().fromJson(jsonString, UserLogin::class.java)
                            if (userLogin != null) {
                                completion(userLogin, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("UserLogin"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> completion(null, SLError.IncorrectEmailOrPassword)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }

    fun socialLogin(socialService: SocialService, accessToken: String, device: String, completion: (userLogin: UserLogin?, error: SLError?) -> Unit) {
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
                completion(null, SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val userLogin = Gson().fromJson(jsonString, UserLogin::class.java)
                            if (userLogin != null) {
                                completion(userLogin, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("UserLogin"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> completion(null, SLError.BadRequest("wrong token format"))
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }
}