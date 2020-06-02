package io.simplelogin.android.utils

import android.content.Context
import com.google.gson.Gson
import io.simplelogin.android.utils.enums.*
import io.simplelogin.android.utils.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private val CONTENT_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

private fun Map<String, Any?>.toRequestBody(): RequestBody {
    val jsonObject = JSONObject()

    for ((key, value) in this) {
        jsonObject.put(key, value)
    }

    return jsonObject.toString().toRequestBody(CONTENT_TYPE_JSON)
}

private fun Exception.notNullLocalizedMessage(): String {
    return localizedMessage ?: return "Null error message"
}

private val client = OkHttpClient()

object SLApiService {
    private lateinit var BASE_URL: String

    fun setUpBaseUrl(context: Context) {
        BASE_URL = SLSharedPreferences.getApiUrl(context)
    }

    //region Login
    fun login(
        email: String,
        password: String,
        device: String,
        completion: (Result<UserLogin>) -> Unit
    ) {
        val requestBody = mapOf(
            "email" to email,
            "password" to password,
            "device" to device
        )
            .toRequestBody()

        val request = Request.Builder()
            .url("$BASE_URL/api/auth/login")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val userLogin = Gson().fromJson(jsonString, UserLogin::class.java)
                            if (userLogin != null) {
                                completion(Result.success(userLogin))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(UserLogin::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    400 -> completion(Result.failure(SLError.IncorrectEmailOrPassword))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun verifyMfa(
        mfaKey: MfaKey,
        mfaToken: String,
        device: String,
        completion: (Result<ApiKey>) -> Unit
    ) {
        val requestBody = mapOf(
            "mfa_token" to mfaToken,
            "mfa_key" to mfaKey.value,
            "device" to device
        )
            .toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/mfa")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val apiKey = Gson().fromJson(jsonString, ApiKey::class.java)
                            if (apiKey != null) {
                                completion(Result.success(apiKey))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(ApiKey::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    400 -> completion(Result.failure(SLError.WrongTotpToken))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun signUp(email: String, password: String, completion: (Result<Unit>) -> Unit) {
        val requestBody = mapOf(
            "email" to email,
            "password" to password
        )
            .toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))

                    400 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val errorMessage = Gson().fromJson(jsonString, ErrorMessage::class.java)
                            if (errorMessage != null) {
                                completion(Result.failure(SLError.BadRequest(errorMessage.value)))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(ErrorMessage::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun verifyEmail(email: Email, code: String, completion: (Result<Unit>) -> Unit) {
        val requestBody = mapOf(
            "email" to email.value,
            "code" to code
        )
            .toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/activate")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))
                    400 -> completion(Result.failure(SLError.WrongVerificationCode))
                    410 -> completion(Result.failure(SLError.ReactivationNeeded))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun reactivate(email: Email, completion: (Result<Unit>) -> Unit) {
        val requestBody = mapOf("email" to email.value).toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/reactivate")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun forgotPassword(email: String, completion: () -> Unit) {
        val requestBody = mapOf("email" to email).toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/forgot_password")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion()
            }

            override fun onResponse(call: Call, response: Response) {
                completion()
            }
        })
    }

    fun fetchUserInfo(apiKey: String, completion: (Result<UserInfo>) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/user_info")
            .header("Authentication", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val userInfo = Gson().fromJson(jsonString, UserInfo::class.java)
                            if (userInfo != null) {
                                completion(Result.success(userInfo))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(UserInfo::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun fetchUserOptions(
        apiKey: String,
        completion: (Result<UserOptions>) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/v3/alias/options")
            .header("Authentication", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val userOptions = Gson().fromJson(jsonString, UserOptions::class.java)
                            if (userOptions != null) {
                                completion(Result.success(userOptions))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(UserOptions::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }
    //endregion

    //region Alias
    fun createAlias(
        apiKey: String,
        prefix: String,
        suffix: String,
        note: String?,
        completion: (Result<Alias>) -> Unit
    ) {
        val requestBody = mapOf(
            "alias_prefix" to prefix,
            "alias_suffix" to suffix,
            "note" to note?.replace("\n", "\\n")
        )
            .toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/alias/custom/new")
            .header("Authentication", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val alias = Gson().fromJson(jsonString, Alias::class.java)
                            if (alias != null) {
                                completion(Result.success(alias))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(Alias::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    409 -> completion(Result.failure(SLError.DuplicatedAlias))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun randomAlias(
        apiKey: String,
        randomMode: RandomMode,
        note: String?,
        completion: (Result<Alias>) -> Unit
    ) {
        val requestBody = mapOf("note" to note?.replace("\n", "\\n")).toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/alias/random/new?mode=${randomMode.parameterName}")
            .header("Authentication", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val alias = Gson().fromJson(jsonString, Alias::class.java)
                            if (alias != null) {
                                completion(Result.success(alias))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(Alias::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }
                    400 -> completion(Result.failure(SLError.CanNotCreateMoreAlias))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun getAlias(
        apiKey: String,
        aliasId: Int,
        completion: (Result<Alias>) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/$aliasId")
            .header("Authentication", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val alias = Gson().fromJson(jsonString, Alias::class.java)
                            if (alias != null) {
                                completion(Result.success(alias))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(Alias::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun fetchAliases(
        apiKey: String,
        page: Int,
        searchTerm: String? = null,
        completion: (Result<List<Alias>>) -> Unit
    ) {

        val request = when (searchTerm) {
            null -> Request.Builder()
                .url("${BASE_URL}/api/v2/aliases?page_id=$page")
                .header("Authentication", apiKey)
                .build()

            else -> {
                val requestBody = mapOf("query" to searchTerm).toRequestBody()
                Request.Builder()
                    .url("${BASE_URL}/api/v2/aliases?page_id=$page")
                    .header("Authentication", apiKey)
                    .post(requestBody)
                    .build()
            }
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            //val aliasListType = object : TypeToken<List<Alias>>() {}.type
                            //val aliases = Gson().fromJson<List<Alias>>(jsonString, aliasListType)
                            val aliasArray = Gson().fromJson(jsonString, AliasArray::class.java)
                            if (aliasArray != null) {
                                completion(Result.success(aliasArray.aliases))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(AliasArray::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    400 -> completion(Result.failure(SLError.PageIdRequired))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun toggleAlias(apiKey: String, alias: Alias, completion: (Result<Enabled>) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/toggle")
            .header("Authentication", apiKey)
            .post("".toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val enabled = Gson().fromJson(jsonString, Enabled::class.java)
                            if (enabled != null) {
                                completion(Result.success(enabled))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(Enabled::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun deleteAlias(apiKey: String, alias: Alias, completion: (Result<Unit>) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}")
            .header("Authentication", apiKey)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun fetchAliasActivities(
        apiKey: String,
        alias: Alias,
        page: Int,
        completion: (Result<List<AliasActivity>>) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/activities?page_id=$page")
            .header("Authentication", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val aliasActivityArray =
                                Gson().fromJson(jsonString, AliasActivityArray::class.java)
                            if (aliasActivityArray != null) {
                                completion(Result.success(aliasActivityArray.activities))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(AliasActivityArray::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    400 -> completion(Result.failure(SLError.PageIdRequired))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun updateAliasNote(
        apiKey: String,
        alias: Alias,
        note: String?,
        completion: (Result<Unit>) -> Unit
    ) {
        val requestBody = mapOf("note" to note).toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}")
            .header("Authentication", apiKey)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    //endregion

    //region Contact
    fun fetchContacts(
        apiKey: String,
        alias: Alias,
        page: Int,
        completion: (Result<List<Contact>>) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/contacts?page_id=$page")
            .header("Authentication", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val contactArray = Gson().fromJson(jsonString, ContactArray::class.java)
                            if (contactArray != null) {
                                completion(Result.success(contactArray.contacts))
                            } else {
                                completion(Result.failure(SLError.FailedToParse(ContactArray::class.java)))
                            }
                        } else {
                            completion(Result.failure(SLError.NoData))
                        }
                    }

                    400 -> completion(Result.failure(SLError.PageIdRequired))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun createContact(
        apiKey: String,
        alias: Alias,
        email: String,
        completion: (Result<Unit>) -> Unit
    ) {
        val requestBody = mapOf("contact" to email).toRequestBody()

        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/contacts")
            .header("Authentication", apiKey)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> completion(Result.success(Unit))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    409 -> completion(Result.failure(SLError.DuplicatedContact))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }

    fun deleteContact(apiKey: String, contact: Contact, completion: (Result<Unit>) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/contacts/${contact.id}")
            .header("Authentication", apiKey)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(Result.failure(SLError.UnknownError(e.notNullLocalizedMessage())))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(Result.success(Unit))
                    401 -> completion(Result.failure(SLError.InvalidApiKey))
                    500 -> completion(Result.failure(SLError.InternalServerError))
                    502 -> completion(Result.failure(SLError.BadGateway))
                    else -> completion(Result.failure(SLError.ResponseError(response.code)))
                }
            }
        })
    }
    //endregion
}