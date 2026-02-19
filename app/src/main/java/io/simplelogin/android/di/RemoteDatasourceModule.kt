package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasourceImpl
import io.simplelogin.android.data.remote.datasource.MailboxesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.MailboxesRemoteDatasourceImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class RemoteDatasourceModule {
    @[Binds Singleton]
    abstract fun bindLogInSignUp(impl: LogInSignUpRemoteDatasourceImpl): LogInSignUpRemoteDatasource

    @[Binds Singleton]
    abstract fun bindAliases(impl: AliasesRemoteDatasourceImpl): AliasesRemoteDatasource

    @[Binds Singleton]
    abstract fun bindCreation(impl: CreationRemoteDatasourceImpl): CreationRemoteDatasource

    @[Binds Singleton]
    abstract fun bindAccountSettings(impl: AccountSettingsRemoteDatasourceImpl): AccountSettingsRemoteDatasource

    @[Binds Singleton]
    abstract fun bindMailboxes(impl: MailboxesRemoteDatasourceImpl): MailboxesRemoteDatasource

    @[Binds Singleton]
    abstract fun bindCustomDomains(impl: CustomDomainsRemoteDatasourceImpl): CustomDomainsRemoteDatasource
}