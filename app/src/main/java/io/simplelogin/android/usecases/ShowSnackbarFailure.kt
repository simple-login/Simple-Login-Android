package io.simplelogin.android.usecases

import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import javax.inject.Inject

interface ShowSnackbarFailureUseCase {
    suspend operator fun invoke(message: String)
}

class ShowSnackbarFailureUseCaseImpl @Inject constructor(private val manager: SnackbarManager) :
    ShowSnackbarFailureUseCase {
    override suspend fun invoke(message: String) =
        manager.showSnackbar(SnackbarConfiguration(message = message, type = SnackbarType.FAILURE))
}