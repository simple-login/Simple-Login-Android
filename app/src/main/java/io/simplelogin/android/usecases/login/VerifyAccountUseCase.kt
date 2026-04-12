package io.simplelogin.android.usecases.login

import io.simplelogin.android.core.model.Result
import io.simplelogin.android.core.model.api.ApiError
import io.simplelogin.android.core.network.MessageResponse
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface VerifyAccountUseCase {
    suspend operator fun invoke(email: String, code: String): Result<MessageResponse, ApiError>
}

class VerifyAccountUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    VerifyAccountUseCase {
    override suspend fun invoke(email: String, code: String): Result<MessageResponse, ApiError> =
        datasource.activate(email = email, code = code)
}