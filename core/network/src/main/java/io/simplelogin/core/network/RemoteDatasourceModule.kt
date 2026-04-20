package io.simplelogin.core.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.core.network.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.core.network.datasource.AccountSettingsRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.AliasDetailsRemoteDatasource
import io.simplelogin.core.network.datasource.AliasDetailsRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.AliasesRemoteDatasource
import io.simplelogin.core.network.datasource.AliasesRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.CreationRemoteDatasource
import io.simplelogin.core.network.datasource.CreationRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.core.network.datasource.CustomDomainsRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasourceImpl
import io.simplelogin.core.network.datasource.MailboxesRemoteDatasource
import io.simplelogin.core.network.datasource.MailboxesRemoteDatasourceImpl
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

    @[Binds Singleton]
    abstract fun bindAliasDetails(impl: AliasDetailsRemoteDatasourceImpl): AliasDetailsRemoteDatasource
}