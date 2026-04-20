package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface ResendActivationCodeUseCase {
    suspend operator fun invoke(email: String): Result<Unit, ApiError>
}

class ResendActivationCodeUseCaseImpl @Inject constructor(val datasource: LogInSignUpRemoteDatasource) :
    ResendActivationCodeUseCase {
    override suspend fun invoke(email: String): Result<Unit, ApiError> =
        datasource.reactivate(email).mapValue {}
}