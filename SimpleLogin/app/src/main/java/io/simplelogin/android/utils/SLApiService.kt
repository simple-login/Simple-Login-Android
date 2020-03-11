package io.simplelogin.android.utils

import com.google.gson.Gson
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.enums.SocialService
import io.simplelogin.android.utils.model.ApiKey
import io.simplelogin.android.utils.model.ErrorMessage
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

    fun verifyMfa(mfaKey: String, mfaToken: String, device: String, completion: (apiKey: ApiKey?, error: SLError?) -> Unit) {
        val body = """
            {
                "mfa_token": "$mfaToken",
                "mfa_key": "$mfaKey",
                "device": "$device"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/mfa")
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
                            val apiKey = Gson().fromJson(jsonString, ApiKey::class.java)
                            if (apiKey != null) {
                                completion(apiKey, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("ApiKey"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val errorMessage = Gson().fromJson(jsonString, ErrorMessage::class.java)
                            if (errorMessage != null) {
                                completion(null, SLError.BadRequest(errorMessage.value))
                            } else {
                                completion(null, SLError.FailedToParseObject("ErrorMessage"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }

    fun signUp(email: String, password: String, completion: (error: SLError?) -> Unit) {
        val body = """
            {
                "email": "$email",
                "password": "$password"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/register")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)

                    400 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val errorMessage = Gson().fromJson(jsonString, ErrorMessage::class.java)
                            if (errorMessage != null) {
                                completion(SLError.BadRequest(errorMessage.value))
                            } else {
                                completion(SLError.FailedToParseObject("ErrorMessage"))
                            }
                        } else {
                            completion(SLError.NoData)
                        }
                    }

                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }

    fun verifyEmail(email: String, code: String, completion: (error: SLError?) -> Unit) {
        val body = """
            {
                "email": "$email",
                "code": "$code"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/activate")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)

                    400 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val errorMessage = Gson().fromJson(jsonString, ErrorMessage::class.java)
                            if (errorMessage != null) {
                                completion(SLError.BadRequest(errorMessage.value))
                            } else {
                                completion(SLError.FailedToParseObject("ErrorMessage"))
                            }
                        } else {
                            completion(SLError.NoData)
                        }
                    }

                    410 -> completion(SLError.ReactivationNeeded)

                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }

    fun reactivate(email: String, completion: (error: SLError?) -> Unit) {
        val body = """
            {
                "email": "$email"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/reactivate")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)

                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }

        })
    }
}