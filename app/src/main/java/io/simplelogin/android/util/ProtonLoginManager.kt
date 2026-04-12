package io.simplelogin.android.util

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow

@Singleton
class ProtonLoginManager @Inject constructor() {
    val pendingApiKey = MutableSharedFlow<String>(extraBufferCapacity = 1)
}