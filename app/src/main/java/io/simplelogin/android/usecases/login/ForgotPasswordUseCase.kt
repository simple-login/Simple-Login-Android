package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.models.api.ApiError
import javax.inject.Inject

interface ForgotPasswordUseCase {
    suspend operator fun invoke(email: String): Result<Unit, ApiError>
}

class ForgotPasswordUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    ForgotPasswordUseCase {
    override suspend fun invoke(email: String): Result<Unit, ApiError> =
        datasource.forgotPassword(email).mapValue {}
}