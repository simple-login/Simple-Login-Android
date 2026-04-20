package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface SignUpUseCase {
    suspend fun invoke(email: String, password: String): Result<Unit, ApiError>
}

class SignUpUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    SignUpUseCase {
    override suspend fun invoke(email: String, password: String): Result<Unit, ApiError> =
        datasource.signUp(email = email, password = password).mapValue { }
}