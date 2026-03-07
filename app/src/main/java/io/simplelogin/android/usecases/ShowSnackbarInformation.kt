package io.simplelogin.android.usecases

import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import javax.inject.Inject

interface ShowSnackbarInformationUseCase {
    suspend operator fun invoke(message: String)
}

class ShowSnackbarInformationUseCaseImpl @Inject constructor(private val manager: SnackbarManager) :
    ShowSnackbarInformationUseCase {
    override suspend fun invoke(message: String) =
        manager.showSnackbar(SnackbarConfiguration(message = message))
}