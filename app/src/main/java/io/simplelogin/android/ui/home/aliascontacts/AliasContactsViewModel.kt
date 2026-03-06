package io.simplelogin.android.ui.home.aliascontacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.PAGE_SIZE
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Contact
import io.simplelogin.android.data.models.ui.ContactUiAction
import io.simplelogin.android.data.remote.datasource.AliasDetailsRemoteDatasource
import io.simplelogin.android.domain.ContactUiActionHandler
import io.simplelogin.android.domain.ContactUiActionResult
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AliasContactsViewModel.Factory::class)
class AliasContactsViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val alias: Alias,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val datasource: AliasDetailsRemoteDatasource,
    private val actionHandler: ContactUiActionHandler,
    private val snackbarManager: SnackbarManager,
    observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    private var apiKey: ApiKey? = null

    val deviceSettings = observeDeviceSettings().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevicePreferences.Default
    )

    private val _stateFlow = MutableStateFlow(AliasContactsState.Default)
    val stateFlow: StateFlow<AliasContactsState> = _stateFlow

    @AssistedFactory
    interface Factory {
        fun create(alias: Alias): AliasContactsViewModel
    }

    fun refresh() {
        _stateFlow.update {
            it.copy(isRefreshing = true, isLoadingMore = false, error = null)
        }
        getContacts(page = 0, process = { it })
    }

    fun retry() {
        _stateFlow.update {
            it.copy(isRefreshing = false, isLoadingMore = true, error = null)
        }
        val currentPage = _stateFlow.value.page
        val currentContacts = _stateFlow.value.contacts
        getContacts(page = currentPage, process = { currentContacts + it })
    }

    fun loadMoreIfNeed() {
        if (!_stateFlow.value.canLoadMore) return
        _stateFlow.update {
            it.copy(isRefreshing = false, isLoadingMore = true, error = null)
        }
        val page = _stateFlow.value.page + 1
        val currentContacts = _stateFlow.value.contacts
        getContacts(page = page, process = { currentContacts + it })
    }

    private fun getContacts(page: Int, process: (List<Contact>) -> List<Contact>) {
        withApiKey { apiKey ->
            datasource.getContacts(apiKey = apiKey, aliasId = alias.id, page = page)
                .fold(onSuccess = { contacts ->
                    val processedContacts = process(contacts)
                    _stateFlow.update {
                        it.copy(
                            contacts = processedContacts,
                            page = page,
                            canLoadMore = contacts.count() <= PAGE_SIZE,
                            isRefreshing = false,
                            isLoadingMore = false
                        )
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(
                            isRefreshing = false,
                            isLoadingMore = false,
                            canLoadMore = false,
                            error = error
                        )
                    }
                })
        }
    }

    fun handleAction(contact: Contact, action: ContactUiAction) {
        withApiKey { apiKey ->
            val result = actionHandler.handleContactAction(
                apiKey = apiKey,
                contact = contact,
                action = action
            )
            when (result) {
                ContactUiActionResult.BLOCKED ->
                    _stateFlow.update { state ->
                        state.copy(
                            contacts = state.contacts.map { existingContact ->
                                if (existingContact.id == contact.id) {
                                    existingContact.copy(blockForward = true)
                                } else {
                                    existingContact
                                }
                            }
                        )
                    }

                ContactUiActionResult.UNBLOCKED ->
                    _stateFlow.update { state ->
                        state.copy(
                            contacts = state.contacts.map { existingContact ->
                                if (existingContact.id == contact.id) {
                                    existingContact.copy(blockForward = false)
                                } else {
                                    existingContact
                                }
                            }
                        )
                    }

                ContactUiActionResult.DELETED ->
                    _stateFlow.update { state ->
                        state.copy(
                            contacts = state.contacts.filterNot { it.id == contact.id }
                        )
                    }

                ContactUiActionResult.NONE -> {}
            }
        }
    }

    fun createContact(email: String) {
        withApiKey { apiKey ->
            loadingState.value = true
            datasource.createContact(apiKey = apiKey, aliasId = alias.id, email = email)
                .fold(onSuccess = { contact ->
                    loadingState.value = false
                    if (contact.existed) {
                        val message = context.getString(R.string.contact_exists, email)
                        snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                    } else {
                        val message = context.getString(R.string.contact_created, email)
                        snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                        refresh()
                    }
                }, onFailure = { error ->
                    loadingState.value = false
                    _stateFlow.update { it.copy(error = error) }
                })
        }
    }

    private fun withApiKey(perform: suspend (ApiKey) -> Unit) {
        viewModelScope.launch {
            apiKey?.let { perform(it) } ?: observeSessionSettings()
                .mapNotNull { it.apiKey }
                .collect {
                    apiKey = it
                    perform(it)
                }
        }
    }
}
