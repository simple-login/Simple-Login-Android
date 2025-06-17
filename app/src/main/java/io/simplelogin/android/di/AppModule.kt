package io.simplelogin.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarManagerImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSnackbarManager(): SnackbarManager =
        SnackbarManagerImpl()

    @AppVersion
    @Provides
    @Singleton
    fun provideAppVersion() = "v${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}"
}