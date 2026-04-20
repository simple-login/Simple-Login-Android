package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.core.common.usecase.CopyToClipboardUseCase
import io.simplelogin.core.common.usecase.CopyToClipboardUseCaseImpl
import io.simplelogin.core.common.usecase.ObserveDeviceSettingsUseCase
import io.simplelogin.core.common.usecase.ObserveDeviceSettingsUseCaseImpl
import io.simplelogin.core.common.usecase.ObserveSessionSettingsUseCase
import io.simplelogin.core.common.usecase.ObserveSessionSettingsUseCaseImpl
import io.simplelogin.core.common.usecase.ShowSnackbarFailureUseCase
import io.simplelogin.core.common.usecase.ShowSnackbarFailureUseCaseImpl
import io.simplelogin.core.common.usecase.ShowSnackbarInformationUseCase
import io.simplelogin.core.common.usecase.ShowSnackbarInformationUseCaseImpl
import io.simplelogin.core.common.usecase.UpdateDeviceSettingsUseCase
import io.simplelogin.core.common.usecase.UpdateDeviceSettingsUseCaseImpl
import io.simplelogin.core.common.usecase.UpdateSessionSettingsUseCase
import io.simplelogin.core.common.usecase.UpdateSessionSettingsUseCaseImpl
import io.simplelogin.feature.auth.usecase.LogOutImpl
import io.simplelogin.feature.auth.usecase.LogOutUseCase
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