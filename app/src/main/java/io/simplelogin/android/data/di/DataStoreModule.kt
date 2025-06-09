package io.simplelogin.android.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.domain.util.Crypto
import io.simplelogin.android.domain.util.CryptoImpl
import io.simplelogin.android.domain.datastore.UserSessionPreferences
import io.simplelogin.android.domain.datastore.UserSessionPreferencesSerializer
import io.simplelogin.android.domain.util.Constants
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideCrypto(): Crypto = CryptoImpl

    @Provides
    @Singleton
    fun provideUserSessionPreferencesSerializer(crypto: Crypto) =
        UserSessionPreferencesSerializer(crypto)

    @Provides
    @Singleton
    fun provideUserSessionPreferencesDataStore(
        @ApplicationContext context: Context,
        userSessionPreferencesSerializer: UserSessionPreferencesSerializer
    ): DataStore<UserSessionPreferences> =
        DataStoreFactory.create(
            serializer = userSessionPreferencesSerializer,
            produceFile = {
                File(context.filesDir, Constants.USER_SESSION_PREFS_FILE_NAME)
            },
            corruptionHandler = ReplaceFileCorruptionHandler {
                UserSessionPreferences()
            }
        )
}