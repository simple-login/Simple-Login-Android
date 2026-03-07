package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.CopyToClipboardUseCaseImpl
import io.simplelogin.android.usecases.login.ForgotPasswordUseCase
import io.simplelogin.android.usecases.login.ForgotPasswordUseCaseImpl
import io.simplelogin.android.usecases.login.LogInUseCase
import io.simplelogin.android.usecases.login.LogInUseCaseImpl
import io.simplelogin.android.usecases.login.LogOutImpl
import io.simplelogin.android.usecases.login.LogOutUseCase
import io.simplelogin.android.usecases.login.ResendActivationCodeUseCase
import io.simplelogin.android.usecases.login.ResendActivationCodeUseCaseImpl
import io.simplelogin.android.usecases.login.SignUpUseCase
import io.simplelogin.android.usecases.login.SignUpUseCaseImpl
import io.simplelogin.android.usecases.login.VerifyMfaUseCase
import io.simplelogin.android.usecases.login.VerifyMfaUseCaseImpl
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCaseImpl
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCaseImpl
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCaseImpl
import io.simplelogin.android.usecases.settings.UpdateDeviceSettingsUseCase
import io.simplelogin.android.usecases.settings.UpdateDeviceSettingsUseCaseImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class UseCaseModule {
    @[Binds Singleton]
    abstract fun bindObserveSessionSettingsUseCase(impl: ObserveSessionSettingsUseCaseImpl): ObserveSessionSettingsUseCase

    @[Binds Singleton]
    abstract fun bindUpdateSessionSettingsUseCase(impl: UpdateSessionSettingsUseCaseImpl): UpdateSessionSettingsUseCase

    @[Binds Singleton]
    abstract fun bindLogInUseCase(impl: LogInUseCaseImpl): LogInUseCase

    @[Binds Singleton]
    abstract fun bindVerifyMfaUseCase(impl: VerifyMfaUseCaseImpl): VerifyMfaUseCase

    @[Binds Singleton]
    abstract fun bindForgotPassword(impl: ForgotPasswordUseCaseImpl): ForgotPasswordUseCase

    @[Binds Singleton]
    abstract fun bindSignUpUseCase(impl: SignUpUseCaseImpl): SignUpUseCase

    @[Binds Singleton]
    abstract fun bindResendActivationCodeUseCase(impl: ResendActivationCodeUseCaseImpl): ResendActivationCodeUseCase

    @[Binds Singleton]
    abstract fun bindCopyToClipboardUseCase(impl: CopyToClipboardUseCaseImpl): CopyToClipboardUseCase

    @[Binds Singleton]
    abstract fun bindObserveDeviceSettingsUseCase(impl: ObserveDeviceSettingsUseCaseImpl): ObserveDeviceSettingsUseCase

    @[Binds Singleton]
    abstract fun bindUpdateDeviceSettingsUseCase(impl: UpdateDeviceSettingsUseCaseImpl): UpdateDeviceSettingsUseCase

    @[Binds Singleton]
    abstract fun bindLogOutUseCase(impl: LogOutImpl): LogOutUseCase
}