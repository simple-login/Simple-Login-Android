package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.network.MessageResponse
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface VerifyAccountUseCase {
    suspend operator fun invoke(email: String, code: String): Result<MessageResponse, ApiError>
}

class VerifyAccountUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    VerifyAccountUseCase {
    override suspend fun invoke(email: String, code: String): Result<MessageResponse, ApiError> =
        datasource.activate(email = email, code = code)
}
