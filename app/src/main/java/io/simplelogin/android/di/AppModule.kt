package io.simplelogin.android.di

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.domain.ActivityUiActionHandler
import io.simplelogin.android.domain.ActivityUiActionHandlerImpl
import io.simplelogin.android.domain.ContactUiActionHandler
import io.simplelogin.android.domain.ContactUiActionHandlerImpl
import io.simplelogin.android.domain.AliasListManager
import io.simplelogin.android.domain.AliasListManagerImpl
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarManagerImpl
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoadingState

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceName

typealias LoadingStateFlow = MutableStateFlow<Boolean>

@[Module InstallIn(SingletonComponent::class)]
abstract class AppModule {
    @[Binds Singleton]
    abstract fun bindAliasListManager(impl: AliasListManagerImpl): AliasListManager

    @[Binds Singleton]
    abstract fun bindActivityUiActionHandler(impl: ActivityUiActionHandlerImpl): ActivityUiActionHandler

    @[Binds Singleton]
    abstract fun bindContactUiActionHandler(impl: ContactUiActionHandlerImpl): ContactUiActionHandler

    companion object {
        @[Provides Singleton]
        fun bindSnackbarManager(): SnackbarManager = SnackbarManagerImpl()

        @[AppVersion Provides Singleton]
        fun provideAppVersion() = "v${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}"

        @[LoadingState Provides Singleton]
        fun provideLoadingState(): LoadingStateFlow = MutableStateFlow(false)

        @[DeviceName Provides Singleton]
        fun provideDeviceName() = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
    }
}