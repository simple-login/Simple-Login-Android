package io.simplelogin.android.data.remote

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.data.models.Alias
import io.simplelogin.android.data.models.AliasActivities
import io.simplelogin.android.data.models.AliasOptions
import io.simplelogin.android.data.models.Aliases
import io.simplelogin.android.data.models.ApiKey
import io.simplelogin.android.data.models.BlockForward
import io.simplelogin.android.data.models.Contact
import io.simplelogin.android.data.models.Contacts
import io.simplelogin.android.data.models.CustomDomains
import io.simplelogin.android.data.models.DeletedAliases
import io.simplelogin.android.data.models.Mailbox
import io.simplelogin.android.data.models.Mailboxes
import io.simplelogin.android.data.models.Stats
import io.simplelogin.android.data.models.UpdateAliasOptions
import io.simplelogin.android.data.models.UpdateCustomDomainOptions
import io.simplelogin.android.data.models.UpdateCustomDomainResponse
import io.simplelogin.android.data.models.UpdateMailboxOptions
import io.simplelogin.android.data.models.UserInfo
import io.simplelogin.android.data.models.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

private const val AUTH_HEADER = "Authentication"
private const val PATH_ID = "id"
private const val PAGE_ID = "page_id"

data class MessageResponse(@SerializedName("msg") val value: String)
data class OkResponse(@SerializedName("ok") val value: Boolean)
data class DeletedResponse(@SerializedName("deleted") val value: Boolean)
data class EnabledResponse(@SerializedName("enabled") val value: Boolean)
data class UpdateResponse(@SerializedName("updated") val value: Boolean)

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

    @POST("api/aliases/${PATH_ID}/contacts")
    suspend fun createContact(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int,
        @Body body: CreateContactBody
        ): Response<Contact>

    @DELETE("api/aliases/${PATH_ID}")
    suspend fun deleteAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int
    ): Response<DeletedResponse>

    @GET("api/aliases/${PATH_ID}/activities")
    suspend fun getAliasActivities(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int,
        @Query(PAGE_ID) pageId: Int
    ): Response<AliasActivities>

    @GET("api/aliases/${PATH_ID}")
    suspend fun getAlias(@Path(PATH_ID) aliasId: Int): Response<Alias>

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

    @GET("api/aliases/${PATH_ID}/contacts")
    suspend fun getContacts(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int,
        @Query(PAGE_ID) pageId: Int
    ): Response<Contacts>

    @POST("api/alias/random/new")
    suspend fun randomAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Query("mode") mode: String, // "uuid" or "word"
        @Query("hostname") hostname: String?,
        @Body body: NoteBody
    ): Response<Alias>

    @POST("api/aliases/${PATH_ID}/toggle")
    suspend fun toggleAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int
    ): Response<EnabledResponse>

    @PATCH("api/aliases/${PATH_ID}")
    suspend fun updateAlias(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) aliasId: Int,
        @Body body: UpdateAliasOptions
    ): Response<OkResponse>

    // Contact
    @DELETE("api/contacts/${PATH_ID}")
    suspend fun deleteContact(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) contactId: Int
    ): Response<DeletedResponse>

    @POST("api/contacts/${PATH_ID}/toggle")
    suspend fun toggleContact(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) contactId: Int
    ): Response<BlockForward>

    // Custom domains
    @GET("api/custom_domains")
    suspend fun getCustomDomains(@Header(AUTH_HEADER) apiKey: String): Response<CustomDomains>

    @GET("api/custom_domains/${PATH_ID}/trash")
    suspend fun getDeletedAliases(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) domainId: Int
    ): Response<DeletedAliases>

    @PATCH("api/custom_domains/${PATH_ID}")
    suspend fun updateCustomDomain(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) domainId: Int,
        @Body body: UpdateCustomDomainOptions
    ): Response<UpdateCustomDomainResponse>

    // Mailbox
    @POST("api/mailboxes")
    suspend fun createMailbox(
        @Header(AUTH_HEADER) apiKey: String,
        @Body body: EmailBody
    ): Response<Mailbox>

    @DELETE("api/mailboxes/${PATH_ID}")
    suspend fun deleteMailbox(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) mailboxId: Int,
        @Body body: TransferAliasesBody
    ): Response<DeletedResponse>

    @GET("api/v2/mailboxes")
    suspend fun getMailboxes(@Header(AUTH_HEADER) apiKey: String): Response<Mailboxes>

    @PUT("api/mailboxes/${PATH_ID}")
    suspend fun updateMailbox(
        @Header(AUTH_HEADER) apiKey: String,
        @Path(PATH_ID) mailboxId: Int,
        @Body body: UpdateMailboxOptions
    ): Response<UpdateResponse>
}