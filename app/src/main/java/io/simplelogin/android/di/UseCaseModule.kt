package io.simplelogin.android.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCaseImpl
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCaseImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class UseCaseModule {
    @[Binds Singleton]
    abstract fun bindObserveSessionSettingsUseCase(impl: ObserveSessionSettingsUseCaseImpl):
            ObserveSessionSettingsUseCase

    @[Binds Singleton]
    abstract fun bindUpdateSessionSettingsUseCase(impl: UpdateSessionSettingsUseCaseImpl):
            UpdateSessionSettingsUseCase
}