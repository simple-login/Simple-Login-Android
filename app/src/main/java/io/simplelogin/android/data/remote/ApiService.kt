package io.simplelogin.android.data.remote

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.data.models.Alias
import io.simplelogin.android.data.models.AliasActivities
import io.simplelogin.android.data.models.AliasID
import io.simplelogin.android.data.models.AliasOptions
import io.simplelogin.android.data.models.Aliases
import io.simplelogin.android.data.models.ApiKey
import io.simplelogin.android.data.models.Contact
import io.simplelogin.android.data.models.Contacts
import io.simplelogin.android.data.models.Stats
import io.simplelogin.android.data.models.UpdateAliasOptions
import io.simplelogin.android.data.models.UserInfo
import io.simplelogin.android.data.models.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

private const val AUTH_HEADER = "Authentication"
private const val ALIAS_ID = "alias_id"
private const val PAGE_ID = "page_id"

data class MessageResponse(@SerializedName("msg") val value: String)
data class OkResponse(@SerializedName("ok") val value: Boolean)
data class DeletedResponse(@SerializedName("deleted") val value: Boolean)
data class EnabledResponse(@SerializedName("enabled") val value: Boolean)

interface ApiService {
    // Account
    @POST("api/auth/activate")
    suspend fun activate(@Body body: ActivateAccountBody): Response<MessageResponse>

    @POST("api/auth/forgot_password")
    suspend fun forgotPassword(@Body body: EmailBody): Response<OkResponse>

    @GET("api/stats")
    suspend fun getStats(): Response<Stats>

    @GET("api/user_info")
    suspend fun getUserInfo(): Response<UserInfo>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginBody): Response<UserLogin>

    @POST("api/auth/mfa")
    suspend fun mfaAuth(@Body body: MfaAuthBody): Response<ApiKey>

    @POST("api/auth/reactivate")
    suspend fun reactivate(@Body body: EmailBody): Response<MessageResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterBody): Response<MessageResponse>

    @PATCH("api/user_info")
    suspend fun updateProfilePicture(@Body body: UpdateProfilePictureBody): Response<UserInfo>

    @PATCH("api/user_info")
    suspend fun updateName(@Body body: UpdateNameBody): Response<UserInfo>

    // Alias
    @POST("api/v3/alias/custom/new")
    suspend fun createAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Body body: CreateAliasBody,
        @Query("hostname") hostname: String?
        ): Response<Alias>

    @POST("api/aliases/${ALIAS_ID}/contacts")
    suspend fun createContact(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID,
        @Body body: CreateContactBody
        ): Response<Contact>

    @DELETE("api/aliases/${ALIAS_ID}")
    suspend fun deleteAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID
    ): Response<DeletedResponse>

    @GET("api/aliases/${ALIAS_ID}/activities")
    suspend fun getAliasActivities(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID,
        @Query(PAGE_ID) pageId: Int
    ): Response<AliasActivities>

    @GET("api/aliases/${ALIAS_ID}")
    suspend fun getAlias(@Path(ALIAS_ID) aliasId: AliasID): Response<Alias>

    @GET("api/v2/aliases")
    suspend fun getAliases(
        @Header(AUTH_HEADER) apiKey: String,
        @Query(PAGE_ID) pageId: Int
    ): Response<Aliases>

    @GET("api/v2/aliases")
    suspend fun filterAliases(
        @Header(AUTH_HEADER) apiKey: String,
        @Query(PAGE_ID) pageId: Int,
        @QueryMap params: Map<String?, String>
    ): Response<Aliases>

    @POST("api/v2/aliases")
    suspend fun searchAliases(
        @Header(AUTH_HEADER) apiKey: String,
        @Query(PAGE_ID) pageId: Int,
        @Body searchBody: SearchBody
    ): Response<Aliases>

    @GET("api/v5/alias/options")
    suspend fun getAliasOptions(@Header(AUTH_HEADER) apiKey: String): Response<AliasOptions>

    @GET("api/aliases/${ALIAS_ID}/contacts")
    suspend fun getContacts(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID,
        @Query(PAGE_ID) pageId: Int
    ): Response<Contacts>

    @POST("api/alias/random/new")
    suspend fun randomAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Query("mode") mode: String, // "uuid" or "word"
        @Query("hostname") hostname: String?,
        @Body body: NoteBody
    ): Response<Alias>

    @POST("api/aliases/${ALIAS_ID}/toggle")
    suspend fun toggleAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID
    ): Response<EnabledResponse>

    @PATCH("api/aliases/${ALIAS_ID}")
    suspend fun updateAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(ALIAS_ID) aliasId: AliasID,
        @Body body: UpdateAliasOptions
    ): Response<OkResponse>
}