package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.core.common.usecase.CopyToClipboardUseCase
import io.simplelogin.android.core.common.usecase.CopyToClipboardUseCaseImpl
import io.simplelogin.android.core.common.usecase.ObserveDeviceSettingsUseCase
import io.simplelogin.android.core.common.usecase.ObserveDeviceSettingsUseCaseImpl
import io.simplelogin.android.core.common.usecase.ObserveSessionSettingsUseCase
import io.simplelogin.android.core.common.usecase.ObserveSessionSettingsUseCaseImpl
import io.simplelogin.android.core.common.usecase.ShowSnackbarFailureUseCase
import io.simplelogin.android.core.common.usecase.ShowSnackbarFailureUseCaseImpl
import io.simplelogin.android.core.common.usecase.ShowSnackbarInformationUseCase
import io.simplelogin.android.core.common.usecase.ShowSnackbarInformationUseCaseImpl
import io.simplelogin.android.core.common.usecase.UpdateDeviceSettingsUseCase
import io.simplelogin.android.core.common.usecase.UpdateDeviceSettingsUseCaseImpl
import io.simplelogin.android.core.common.usecase.UpdateSessionSettingsUseCase
import io.simplelogin.android.core.common.usecase.UpdateSessionSettingsUseCaseImpl
import io.simplelogin.android.feature.auth.usecase.LogOutImpl
import io.simplelogin.android.feature.auth.usecase.LogOutUseCase
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class UseCaseModule {
    @[Binds Singleton]
    abstract fun bindObserveSessionSettingsUseCase(impl: ObserveSessionSettingsUseCaseImpl): ObserveSessionSettingsUseCase

    @[Binds Singleton]
    abstract fun bindUpdateSessionSettingsUseCase(impl: UpdateSessionSettingsUseCaseImpl): UpdateSessionSettingsUseCase

    @[Binds Singleton]
    abstract fun bindCopyToClipboardUseCase(impl: CopyToClipboardUseCaseImpl): CopyToClipboardUseCase

    @[Binds Singleton]
    abstract fun bindObserveDeviceSettingsUseCase(impl: ObserveDeviceSettingsUseCaseImpl): ObserveDeviceSettingsUseCase

    @[Binds Singleton]
    abstract fun bindUpdateDeviceSettingsUseCase(impl: UpdateDeviceSettingsUseCaseImpl): UpdateDeviceSettingsUseCase

    @[Binds Singleton]
    abstract fun bindLogOutUseCase(impl: LogOutImpl): LogOutUseCase

    @[Binds Singleton]
    abstract fun bindShowSnackbarInformation(impl: ShowSnackbarInformationUseCaseImpl): ShowSnackbarInformationUseCase

    @[Binds Singleton]
    abstract fun bindShowSnackbarFailure(impl: ShowSnackbarFailureUseCaseImpl): ShowSnackbarFailureUseCase
}