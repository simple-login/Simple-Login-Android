package io.simplelogin.android.usecases.session

import androidx.datastore.core.DataStore
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
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