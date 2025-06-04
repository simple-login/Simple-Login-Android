package io.simplelogin.android.data.remote

import io.simplelogin.android.data.models.UserLogin
import io.simplelogin.android.data.remote.request_body.ActivateAccountBody
import io.simplelogin.android.data.remote.request_body.LoginBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    // Account
    @POST("api/auth/activate")
    suspend fun activate(@Body body: ActivateAccountBody): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginBody): Response<UserLogin>

    // Alias
    @GET("api/aliases/{id}")
    suspend fun getAlias(@Path("id") aliasId: Int)
}