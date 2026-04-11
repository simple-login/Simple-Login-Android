package io.simplelogin.android.data.remote

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.api.Token
import io.simplelogin.android.data.models.api.UpdateAliasOption
import io.simplelogin.android.data.models.api.UpdateCustomDomainOption
import io.simplelogin.android.data.models.api.UpdateCustomDomainResponse
import io.simplelogin.android.data.models.api.UpdateMailboxOption
import io.simplelogin.android.data.models.api.UpdateUserInfoOption
import io.simplelogin.android.data.models.api.UpdateUserSettingsOption
import io.simplelogin.android.data.models.api.UsableDomain
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.api.UserLogin
import io.simplelogin.android.data.models.api.UserSettings
import io.simplelogin.android.models.api.Alias
import io.simplelogin.android.models.api.AliasActivities
import io.simplelogin.android.models.api.AliasId
import io.simplelogin.android.models.api.AliasOptions
import io.simplelogin.android.models.api.Aliases
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.BlockForward
import io.simplelogin.android.models.api.Contact
import io.simplelogin.android.models.api.Contacts
import io.simplelogin.android.models.api.CustomDomains
import io.simplelogin.android.models.api.DeletedAliases
import io.simplelogin.android.models.api.Mailbox
import io.simplelogin.android.models.api.Mailboxes
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

private const val AUTH_HEADER = "Authentication"
private const val PAGE_ID = "page_id"

data class MessageResponse(@SerializedName("msg") val value: String)
data class OkResponse(@SerializedName("ok") val value: Boolean)
data class DeletedResponse(@SerializedName("deleted") val value: Boolean)
data class EnabledResponse(@SerializedName("enabled") val value: Boolean)
data class UpdateResponse(@SerializedName("updated") val value: Boolean)

// https://github.com/simple-login/app/blob/master/docs/api.md
interface ApiService {
    // Account
    @POST("api/auth/activate")
    suspend fun activate(@Body body: ActivateAccountBody): Response<MessageResponse>

    @POST("api/auth/forgot_password")
    suspend fun forgotPassword(@Body body: EmailBody): Response<OkResponse>

    @GET("api/stats")
    suspend fun getStats(@Header(AUTH_HEADER) apiKey: ApiKey): Response<Stats>

    @GET("api/user_info")
    suspend fun getUserInfo(@Header(AUTH_HEADER) apiKey: ApiKey): Response<UserInfo>

    @PATCH("api/user_info")
    suspend fun updateUserInfo(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Body body: UpdateUserInfoOption
    ): Response<UserInfo>

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

    @DELETE("api/setting/unlink_proton_account")
    suspend fun unlinkProton(@Header(AUTH_HEADER) apiKey: ApiKey): Response<OkResponse>

    // Alias
    @POST("api/v3/alias/custom/new")
    suspend fun createAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Body body: CreateAliasBody,
        @Query("hostname") hostname: String?
    ): Response<Alias>

    @POST("api/aliases/{id}/contacts")
    suspend fun createContact(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId,
        @Body body: CreateContactBody
    ): Response<Contact>

    @DELETE("api/aliases/{id}")
    suspend fun deleteAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId
    ): Response<DeletedResponse>

    @GET("api/aliases/{id}/activities")
    suspend fun getAliasActivities(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId,
        @Query(PAGE_ID) pageId: Int
    ): Response<AliasActivities>

    @GET("api/aliases/{id}")
    suspend fun getAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId
    ): Response<Alias>

    @GET("api/v2/aliases")
    suspend fun getAliases(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Query(PAGE_ID) pageId: Int
    ): Response<Aliases>

    @GET("api/v2/aliases")
    suspend fun filterAliases(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Query(PAGE_ID) pageId: Int,
        @QueryMap params: Map<String?, String>
    ): Response<Aliases>

    @POST("api/v2/aliases")
    suspend fun searchAliases(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Query(PAGE_ID) pageId: Int,
        @Body searchBody: SearchBody
    ): Response<Aliases>

    @GET("api/v5/alias/options")
    suspend fun getAliasOptions(@Header(AUTH_HEADER) apiKey: ApiKey): Response<AliasOptions>

    @GET("api/aliases/{id}/contacts")
    suspend fun getContacts(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId,
        @Query(PAGE_ID) pageId: Int
    ): Response<Contacts>

    @POST("api/alias/random/new")
    suspend fun randomAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Query("mode") mode: String, // "uuid" or "word"
        @Query("hostname") hostname: String?,
        @Body body: NoteBody
    ): Response<Alias>

    @POST("api/aliases/{id}/toggle")
    suspend fun toggleAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId
    ): Response<EnabledResponse>

    @PATCH("api/aliases/{id}")
    suspend fun updateAlias(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") aliasId: AliasId,
        @Body body: UpdateAliasOption
    ): Response<OkResponse>

    // Contact
    @DELETE("api/contacts/{id}")
    suspend fun deleteContact(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") contactId: Int
    ): Response<DeletedResponse>

    @POST("api/contacts/{id}/toggle")
    suspend fun toggleContact(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") contactId: Int
    ): Response<BlockForward>

    // Custom domains
    @GET("api/custom_domains")
    suspend fun getCustomDomains(@Header(AUTH_HEADER) apiKey: ApiKey): Response<CustomDomains>

    @GET("api/custom_domains/{id}/trash")
    suspend fun getDeletedAliases(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") domainId: Int
    ): Response<DeletedAliases>

    @PATCH("api/custom_domains/{id}")
    suspend fun updateCustomDomain(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") domainId: Int,
        @Body body: UpdateCustomDomainOption
    ): Response<UpdateCustomDomainResponse>

    // Mailbox
    @POST("api/mailboxes")
    suspend fun createMailbox(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Body body: EmailBody
    ): Response<Mailbox>

    @HTTP(method = "DELETE", path = "api/mailboxes/{id}", hasBody = true)
    suspend fun deleteMailbox(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") mailboxId: Int,
        @Body body: TransferAliasesBody
    ): Response<DeletedResponse>

    @GET("api/v2/mailboxes")
    suspend fun getMailboxes(@Header(AUTH_HEADER) apiKey: ApiKey): Response<Mailboxes>

    @PUT("api/mailboxes/{id}")
    suspend fun updateMailbox(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Path("id") mailboxId: Int,
        @Body body: UpdateMailboxOption
    ): Response<UpdateResponse>

    // Misc
    @DELETE("api/user")
    suspend fun deleteUser(@Header(AUTH_HEADER) apiKey: ApiKey): Response<OkResponse>

    @PATCH("api/sudo")
    suspend fun enterSudoMode(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Body body: PasswordBody
    ): Response<OkResponse>

    @GET("api/user/cookie_token")
    suspend fun getCookieToken(@Header(AUTH_HEADER) apiKey: ApiKey): Response<Token>

    // Settings
    @GET("api/v2/setting/domains")
    suspend fun getUsableDomains(@Header(AUTH_HEADER) apiKey: ApiKey): Response<List<UsableDomain>>

    @GET("api/setting")
    suspend fun getUserSettings(@Header(AUTH_HEADER) apiKey: ApiKey): Response<UserSettings>

    @DELETE("api/setting/unlink_proton_account")
    suspend fun unlinkProtonAccount(@Header(AUTH_HEADER) apiKey: ApiKey): Response<OkResponse>

    @PATCH("api/setting")
    suspend fun updateUserSettings(
        @Header(AUTH_HEADER) apiKey: ApiKey,
        @Body body: UpdateUserSettingsOption
    ): Response<UserSettings>
}