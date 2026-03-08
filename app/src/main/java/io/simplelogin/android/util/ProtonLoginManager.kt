package io.simplelogin.android.util

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtonLoginManager @Inject constructor() {
    val pendingApiKey = MutableSharedFlow<String>(extraBufferCapacity = 1)
}