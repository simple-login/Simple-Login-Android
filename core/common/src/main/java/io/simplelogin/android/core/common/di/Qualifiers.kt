package io.simplelogin.android.core.common.di

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppVersion

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LoadingState

typealias LoadingStateFlow = MutableStateFlow<Boolean>

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeviceName