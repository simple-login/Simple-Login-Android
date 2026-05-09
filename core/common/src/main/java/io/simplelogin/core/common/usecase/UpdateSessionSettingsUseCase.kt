package io.simplelogin.core.common.usecase

import androidx.datastore.core.DataStore
import io.simplelogin.core.model.preferences.UserSessionPreferences
import javax.inject.Inject

interface UpdateSessionSettingsUseCase {
    suspend operator fun invoke(transform: (UserSessionPreferences) -> UserSessionPreferences)
}

class UpdateSessionSettingsUseCaseImpl @Inject constructor(private val dataStore: DataStore<UserSessionPreferences>) :
    UpdateSessionSettingsUseCase {
    override suspend operator fun invoke(transform: (UserSessionPreferences) -> UserSessionPreferences) {
        dataStore.updateData(transform)
    }
}
