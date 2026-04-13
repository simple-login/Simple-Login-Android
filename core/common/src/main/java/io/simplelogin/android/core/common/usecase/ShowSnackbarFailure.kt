package io.simplelogin.android.core.common.usecase

import io.simplelogin.android.core.designsystem.snackbar.SnackbarConfiguration
import io.simplelogin.android.core.designsystem.snackbar.SnackbarManager
import io.simplelogin.android.core.designsystem.snackbar.SnackbarType
import javax.inject.Inject

interface ShowSnackbarFailureUseCase {
    suspend operator fun invoke(message: String)
}

class ShowSnackbarFailureUseCaseImpl @Inject constructor(private val manager: SnackbarManager) :
    ShowSnackbarFailureUseCase {
    override suspend fun invoke(message: String) =
        manager.showSnackbar(SnackbarConfiguration(message = message, type = SnackbarType.FAILURE))
}