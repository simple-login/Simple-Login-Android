package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.CustomDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@HiltViewModel(assistedFactory = CustomDomainDetailsViewModel.Factory::class)
class CustomDomainDetailsViewModel @AssistedInject constructor(
    @Assisted private val domain: CustomDomain
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(domain: CustomDomain): CustomDomainDetailsViewModel
    }

    private val _domainStateFlow = MutableStateFlow(domain)
    val domainStateFlow: StateFlow<CustomDomain> = _domainStateFlow
}