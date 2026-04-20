package io.simplelogin.feature.auth.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.feature.auth.usecase.ForgotPasswordUseCase
import io.simplelogin.feature.auth.usecase.ForgotPasswordUseCaseImpl
import io.simplelogin.feature.auth.usecase.LogInUseCase
import io.simplelogin.feature.auth.usecase.LogInUseCaseImpl
import io.simplelogin.feature.auth.usecase.ResendActivationCodeUseCase
import io.simplelogin.feature.auth.usecase.ResendActivationCodeUseCaseImpl
import io.simplelogin.feature.auth.usecase.SignUpUseCase
import io.simplelogin.feature.auth.usecase.SignUpUseCaseImpl
import io.simplelogin.feature.auth.usecase.VerifyAccountUseCase
import io.simplelogin.feature.auth.usecase.VerifyAccountUseCaseImpl
import io.simplelogin.feature.auth.usecase.VerifyMfaUseCase
import io.simplelogin.feature.auth.usecase.VerifyMfaUseCaseImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
internal abstract class AuthUseCaseModule {
    @[Binds Singleton]
    abstract fun bindLogInUseCase(impl: LogInUseCaseImpl): LogInUseCase

    @[Binds Singleton]
    abstract fun bindVerifyMfaUseCase(impl: VerifyMfaUseCaseImpl): VerifyMfaUseCase

    @[Binds Singleton]
    abstract fun bindVerifyAccountUseCase(impl: VerifyAccountUseCaseImpl): VerifyAccountUseCase

    @[Binds Singleton]
    abstract fun bindForgotPassword(impl: ForgotPasswordUseCaseImpl): ForgotPasswordUseCase

    @[Binds Singleton]
    abstract fun bindSignUpUseCase(impl: SignUpUseCaseImpl): SignUpUseCase

    @[Binds Singleton]
    abstract fun bindResendActivationCodeUseCase(impl: ResendActivationCodeUseCaseImpl): ResendActivationCodeUseCase
}