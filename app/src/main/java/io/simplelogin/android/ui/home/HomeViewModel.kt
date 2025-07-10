package io.simplelogin.android.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeSessionSettings: ObserveSessionSettingsUseCase
): ViewModel() {
}