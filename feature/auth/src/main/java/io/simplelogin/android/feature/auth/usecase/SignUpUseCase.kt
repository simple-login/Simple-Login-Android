package io.simplelogin.android.feature.auth.usecase

import io.simplelogin.android.core.model.Result
import io.simplelogin.android.core.model.api.ApiError
import io.simplelogin.android.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface SignUpUseCase {
    suspend fun invoke(email: String, password: String): Result<Unit, ApiError>
}

class SignUpUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    SignUpUseCase {
    override suspend fun invoke(email: String, password: String): Result<Unit, ApiError> =
        datasource.signUp(email = email, password = password).mapValue { }
}