package io.simplelogin.core.designsystem.snackbar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface SnackbarManager {
    val configuration: Flow<SnackbarConfiguration>

    suspend fun showSnackbar(configuration: SnackbarConfiguration)
}

class SnackbarManagerImpl : SnackbarManager {
    private val _configuration = MutableSharedFlow<SnackbarConfiguration>()

    override val configuration: Flow<SnackbarConfiguration>
        get() = _configuration.asSharedFlow()

    override suspend fun showSnackbar(configuration: SnackbarConfiguration) {
        _configuration.emit(configuration)
    }
}
