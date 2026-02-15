package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasourceImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class RemoteDatasourceModule {
    @[Binds Singleton]
    abstract fun bindLogInSignUp(impl: LogInSignUpRemoteDatasourceImpl): LogInSignUpRemoteDatasource

    @[Binds Singleton]
    abstract fun bindAliases(impl: AliasesRemoteDatasourceImpl): AliasesRemoteDatasource

    @[Binds Singleton]
    abstract fun bindCreation(impl: CreationRemoteDatasourceImpl): CreationRemoteDatasource
}