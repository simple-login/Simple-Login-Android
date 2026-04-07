package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.di.DeviceName
import io.simplelogin.android.models.api.ApiError
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