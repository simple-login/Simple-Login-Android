package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasourceImpl
import io.simplelogin.android.usecases.login.ForgotPasswordUseCase
import io.simplelogin.android.usecases.login.ForgotPasswordUseCaseImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class RemoteDatasourceModule {
    @[Binds Singleton]
    abstract fun bindLogInSignUp(impl: LogInSignUpRemoteDatasourceImpl): LogInSignUpRemoteDatasource

    @[Binds Singleton]
    abstract fun bindForgotPassword(impl: ForgotPasswordUseCaseImpl): ForgotPasswordUseCase
}