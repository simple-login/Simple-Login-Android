package io.simplelogin.android.util

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow

@Singleton
class ProtonLinkManager @Inject constructor() {
    val linkedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
}
