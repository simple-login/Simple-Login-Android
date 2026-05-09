package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface ForgotPasswordUseCase {
    suspend operator fun invoke(email: String): Result<Unit, ApiError>
}

class ForgotPasswordUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    ForgotPasswordUseCase {
    override suspend fun invoke(email: String): Result<Unit, ApiError> =
        datasource.forgotPassword(email).mapValue {}
}
