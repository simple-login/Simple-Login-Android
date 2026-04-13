package io.simplelogin.android.core.common.usecase

import io.simplelogin.android.core.designsystem.snackbar.SnackbarConfiguration
import io.simplelogin.android.core.designsystem.snackbar.SnackbarManager
import javax.inject.Inject

interface ShowSnackbarInformationUseCase {
    suspend operator fun invoke(message: String)
}

class ShowSnackbarInformationUseCaseImpl @Inject constructor(private val manager: SnackbarManager) :
    ShowSnackbarInformationUseCase {
    override suspend fun invoke(message: String) =
        manager.showSnackbar(SnackbarConfiguration(message = message))
}