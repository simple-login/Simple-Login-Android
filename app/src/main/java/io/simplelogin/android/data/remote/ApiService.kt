package io.simplelogin.android.data.remote

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.data.models.ApiKey
import io.simplelogin.android.data.models.Stats
import io.simplelogin.android.data.models.UserInfo
import io.simplelogin.android.data.models.UserLogin
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

data class MessageResponse(
    @SerializedName("msg") val value: String
)

data class OkResponse(
    @SerializedName("ok") val value: Boolean
)

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
    @GET("api/aliases/{id}")
    suspend fun getAlias(@Path("id") aliasId: Int)
}