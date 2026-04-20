package io.simplelogin.core.common

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtonLinkManager @Inject constructor() {
    val linkedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
}