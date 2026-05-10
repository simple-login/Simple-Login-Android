package io.simplelogin.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.core.model.api.UpdateAliasOption
import io.simplelogin.core.model.api.UpdateAliasOptionSerializer
import io.simplelogin.core.model.api.UpdateCustomDomainOption
import io.simplelogin.core.model.api.UpdateCustomDomainOptionSerializer
import io.simplelogin.core.model.api.UpdateMailboxOption
import io.simplelogin.core.model.api.UpdateMailboxOptionSerializer
import io.simplelogin.core.model.api.UpdateUserInfoOption
import io.simplelogin.core.model.api.UpdateUserInfoOptionSerializer
import io.simplelogin.core.model.api.UpdateUserSettingsOption
import io.simplelogin.core.model.api.UpdateUserSettingsOptionSerializer
import jakarta.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .registerTypeAdapter(UpdateUserInfoOption::class.java, UpdateUserInfoOptionSerializer())
        .registerTypeAdapter(
            UpdateUserSettingsOption::class.java,
            UpdateUserSettingsOptionSerializer()
        )
        .registerTypeAdapter(UpdateAliasOption::class.java, UpdateAliasOptionSerializer())
        .registerTypeAdapter(
            UpdateCustomDomainOption::class.java,
            UpdateCustomDomainOptionSerializer()
        )
        .registerTypeAdapter(UpdateMailboxOption::class.java, UpdateMailboxOptionSerializer())
        .serializeNulls()
        .create()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideHeaderInterceptor(): Interceptor =
        Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
            if (originalRequest.body != null && originalRequest.header("Content-Type") == null) {
                requestBuilder.header("Content-Type", "application/json")
            }
            chain.proceed(requestBuilder.build())
        }

    @Provides
    @Singleton
    fun provideBaseUrlProvider(): BaseUrlProvider =
        BaseUrlProviderImpl

    @Provides
    @Singleton
    fun provideDynamicBaseUrlInterceptor(baseUrlProvider: BaseUrlProvider): DynamicBaseUrlInterceptor =
        DynamicBaseUrlInterceptor(baseUrlProvider)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        headerInterceptor: Interceptor,
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(dynamicBaseUrlInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://app.simplelogin.io")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
