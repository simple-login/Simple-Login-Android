package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.remote.MessageResponse
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface VerifyAccountUseCase {
    suspend operator fun invoke(email: String, code: String): Result<MessageResponse, ApiError>
}

class VerifyAccountUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    VerifyAccountUseCase {
    override suspend fun invoke(email: String, code: String): Result<MessageResponse, ApiError> =
        datasource.activate(email = email, code = code)
}