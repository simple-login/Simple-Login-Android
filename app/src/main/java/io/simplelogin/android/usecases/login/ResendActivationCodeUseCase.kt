package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface ResendActivationCodeUseCase {
    suspend fun invoke(email: String): Result<Unit, ApiError>
}

class ResendActivationCodeUseCaseImpl @Inject constructor(val datasource: LogInSignUpRemoteDatasource) :
    ResendActivationCodeUseCase {
    override suspend fun invoke(email: String): Result<Unit, ApiError> =
        datasource.reactivate(email).mapValue {}
}