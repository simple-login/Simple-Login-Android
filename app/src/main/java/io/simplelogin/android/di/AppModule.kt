package io.simplelogin.android.di

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.BuildConfig
import io.simplelogin.core.common.di.AppVersion
import io.simplelogin.core.common.di.DeviceName
import io.simplelogin.core.common.di.LoadingState
import io.simplelogin.core.common.di.LoadingStateFlow
import io.simplelogin.core.designsystem.snackbar.SnackbarManager
import io.simplelogin.core.designsystem.snackbar.SnackbarManagerImpl
import io.simplelogin.android.domain.ActivityUiActionHandler
import io.simplelogin.android.domain.ActivityUiActionHandlerImpl
import io.simplelogin.android.domain.ContactUiActionHandler
import io.simplelogin.android.domain.ContactUiActionHandlerImpl
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class AppModule {
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
