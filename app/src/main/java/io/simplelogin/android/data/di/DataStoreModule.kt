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
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.data.util.Crypto
import io.simplelogin.android.data.util.CryptoImpl
import io.simplelogin.android.data.util.EncryptingSerializer
import java.io.File
import javax.inject.Singleton

private typealias UserSessionPreferencesSerializer = EncryptingSerializer<UserSessionPreferences>

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideCrypto(): Crypto = CryptoImpl

    @Provides
    @Singleton
    fun provideUserSessionPreferencesSerializer(crypto: Crypto): UserSessionPreferencesSerializer =
        EncryptingSerializer(
            crypto,
            UserSessionPreferences.serializer(),
            UserSessionPreferences()
            )

    @Provides
    @Singleton
    fun provideUserSessionPreferencesDataStore(
        @ApplicationContext context: Context,
        serializer: UserSessionPreferencesSerializer
    ): DataStore<UserSessionPreferences> =
        DataStoreFactory.create(
            serializer = serializer,
            produceFile = {
                File(context.filesDir, Constants.USER_SESSION_PREFS_FILE_NAME)
            },
            corruptionHandler = ReplaceFileCorruptionHandler {
                UserSessionPreferences()
            }
        )
}