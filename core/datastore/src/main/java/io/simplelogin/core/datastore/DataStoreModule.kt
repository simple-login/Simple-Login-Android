package io.simplelogin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.simplelogin.core.model.Constants
import io.simplelogin.core.model.preferences.DevicePreferences
import io.simplelogin.core.model.preferences.UserSessionPreferences
import java.io.File
import javax.inject.Singleton

private typealias UserSessionPreferencesSerializer = EncryptingSerializer<UserSessionPreferences>
private typealias DevicePreferencesSerializer = EncryptingSerializer<DevicePreferences>

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

    @Provides
    @Singleton
    fun provideDevicePreferencesSerializer(crypto: Crypto): DevicePreferencesSerializer =
        EncryptingSerializer(
            crypto,
            DevicePreferences.serializer(),
            DevicePreferences()
        )

    @Provides
    @Singleton
    fun provideDevicePreferencesDataStore(
        @ApplicationContext context: Context,
        serializer: DevicePreferencesSerializer
    ): DataStore<DevicePreferences> =
        DataStoreFactory.create(
            serializer = serializer,
            produceFile = {
                File(context.filesDir, Constants.DEVICE_PREFS_FILE_NAME)
            },
            corruptionHandler = ReplaceFileCorruptionHandler {
                DevicePreferences()
            }
        )
}
