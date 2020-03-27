package io.simplelogin.android.utils

import com.google.gson.Gson
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.utils.enums.RandomMode
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.enums.SocialService
import io.simplelogin.android.utils.model.*
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

    //region Login
    fun login(
        email: String,
        password: String,
        device: String,
        completion: (userLogin: UserLogin?, error: SLError?) -> Unit
    ) {
        val body = """
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
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun socialLogin(
        socialService: SocialService,
        accessToken: String,
        device: String,
        completion: (userLogin: UserLogin?, error: SLError?) -> Unit
    ) {
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
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun verifyMfa(
        mfaKey: String,
        mfaToken: String,
        device: String,
        completion: (apiKey: ApiKey?, error: SLError?) -> Unit
    ) {
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

                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
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

                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
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
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
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
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun forgotPassword(email: String, completion: () -> Unit) {
        val body = """
            {
                "email": "$email"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/auth/forgot_password")
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
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

    fun fetchUserInfo(apiKey: String, completion: (userInfo: UserInfo?, error: SLError?) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/user_info")
            .header("Authentication", apiKey)
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
                            val userInfo = Gson().fromJson(jsonString, UserInfo::class.java)
                            if (userInfo != null) {
                                completion(userInfo, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("UserInfo"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun fetchUserOptions(
        apiKey: String,
        completion: (userOptions: UserOptions?, error: SLError?) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/v2/alias/options")
            .header("Authentication", apiKey)
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
                            val userOptions = Gson().fromJson(jsonString, UserOptions::class.java)
                            if (userOptions != null) {
                                completion(userOptions, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("UserOptions"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
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
        completion: (alias: Alias?, error: SLError?) -> Unit
    ) {
        val formattedNote = note?.replace("\n", "\\n")
        val body = """
            {
                "alias_prefix": "$prefix",
                "alias_suffix": "$suffix",
                "note": "$formattedNote"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/alias/custom/new")
            .header("Authentication", apiKey)
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val alias = Gson().fromJson(jsonString, Alias::class.java)
                            if (alias != null) {
                                completion(alias, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("Alias"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }
                    401 -> completion(null, SLError.InvalidApiKey)
                    409 -> completion(null, SLError.DuplicatedAlias)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun randomAlias(
        apiKey: String,
        randomMode: RandomMode,
        note: String?,
        completion: (alias: Alias?, error: SLError?) -> Unit
    ) {
        val formattedNote = note?.replace("\n", "\\n")
        val body = """
            {
                "note": "$formattedNote"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/alias/random/new?mode=${randomMode.parameterName}")
            .header("Authentication", apiKey)
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> {
                        val jsonString = response.body?.string()

                        if (jsonString != null) {
                            val alias = Gson().fromJson(jsonString, Alias::class.java)
                            if (alias != null) {
                                completion(alias, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("Alias"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }
                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun fetchAliases(
        apiKey: String,
        page: Int,
        searchTerm: String? = null,
        completion: (aliases: List<Alias>?, error: SLError?) -> Unit
    ) {

        val request = when (searchTerm) {
            null -> Request.Builder()
                .url("${BASE_URL}/api/aliases?page_id=$page")
                .header("Authentication", apiKey)
                .build()

            else -> {
                val body = """
                    {
                        "query": "$searchTerm"
                    }
                """.trimIndent()
                Request.Builder()
                    .url("${BASE_URL}/api/aliases?page_id=$page")
                    .header("Authentication", apiKey)
                    .post(body.toRequestBody(CONTENT_TYPE_JSON))
                    .build()
            }
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, SLError.UnknownError(e.localizedMessage))
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
                                completion(aliasArray.aliases, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("Alias"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> completion(null, SLError.PageIdRequired)
                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun toggleAlias(
        apiKey: String,
        alias: Alias,
        completion: (enabled: Boolean?, error: SLError?) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/toggle")
            .header("Authentication", apiKey)
            .post("".toRequestBody(CONTENT_TYPE_JSON))
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
                            val enabled = Gson().fromJson(jsonString, Enabled::class.java)
                            if (enabled != null) {
                                completion(enabled.value, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("Enabled"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun deleteAlias(apiKey: String, alias: Alias, completion: (error: SLError?) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}")
            .header("Authentication", apiKey)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)
                    401 -> completion(SLError.InvalidApiKey)
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun fetchAliasActivities(
        apiKey: String,
        alias: Alias,
        page: Int,
        completion: (aliasActivities: List<AliasActivity>?, error: SLError?) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/activities?page_id=$page")
            .header("Authentication", apiKey)
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
                            val aliasActivityArray =
                                Gson().fromJson(jsonString, AliasActivityArray::class.java)
                            if (aliasActivityArray != null) {
                                completion(aliasActivityArray.activities, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("AliasActivityArray"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> completion(null, SLError.PageIdRequired)
                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun updateAliasNote(
        apiKey: String,
        alias: Alias,
        note: String?,
        completion: (error: SLError?) -> Unit
    ) {
        val formattedNote = note?.replace("\n", "\\n")
        val body = """
            {
                "note": "$formattedNote"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}")
            .header("Authentication", apiKey)
            .put(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)
                    401 -> completion(SLError.InvalidApiKey)
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
                    else -> completion(SLError.UnknownError("error code ${response.code}"))
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
        completion: (contacts: List<Contact>?, error: SLError?) -> Unit
    ) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/contacts?page_id=$page")
            .header("Authentication", apiKey)
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
                            val contactArray = Gson().fromJson(jsonString, ContactArray::class.java)
                            if (contactArray != null) {
                                completion(contactArray.contacts, null)
                            } else {
                                completion(null, SLError.FailedToParseObject("ContactArray"))
                            }
                        } else {
                            completion(null, SLError.NoData)
                        }
                    }

                    400 -> completion(null, SLError.PageIdRequired)
                    401 -> completion(null, SLError.InvalidApiKey)
                    500 -> completion(null, SLError.InternalServerError)
                    502 -> completion(null, SLError.BadGateway)
                    else -> completion(null, SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun createContact(
        apiKey: String,
        alias: Alias,
        email: String,
        completion: (error: SLError?) -> Unit
    ) {
        val body = """
            {
                "contact": "$email"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${BASE_URL}/api/aliases/${alias.id}/contacts")
            .header("Authentication", apiKey)
            .post(body.toRequestBody(CONTENT_TYPE_JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    201 -> completion(null)
                    401 -> completion(SLError.InvalidApiKey)
                    409 -> completion(SLError.DuplicatedContact)
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }

    fun deleteContact(apiKey: String, contact: Contact, completion: (error: SLError?) -> Unit) {
        val request = Request.Builder()
            .url("${BASE_URL}/api/contacts/${contact.id}")
            .header("Authentication", apiKey)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(SLError.UnknownError(e.localizedMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                when (response.code) {
                    200 -> completion(null)
                    401 -> completion(SLError.InvalidApiKey)
                    500 -> completion(SLError.InternalServerError)
                    502 -> completion(SLError.BadGateway)
                    else -> completion(SLError.UnknownError("error code ${response.code}"))
                }
            }
        })
    }
    //endregion
}