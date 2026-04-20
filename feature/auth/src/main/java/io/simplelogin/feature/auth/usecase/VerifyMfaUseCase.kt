package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.common.di.DeviceName
import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

interface VerifyMfaUseCase {
    suspend operator fun invoke(token: String, key: String): Result<ApiKey, ApiError>
}

class VerifyMfaUseCaseImpl @Inject constructor(
    private val datasource: LogInSignUpRemoteDatasource,
    @DeviceName private val deviceName: String
) : VerifyMfaUseCase {
    override suspend fun invoke(token: String, key: String) =
        datasource.mfaAuth(token = token, key = key, deviceName = deviceName)
}