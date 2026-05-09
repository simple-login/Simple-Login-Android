package io.simplelogin.android.root

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.core.common.di.AppVersion
import io.simplelogin.core.common.usecase.ObserveSessionSettingsUseCase
import io.simplelogin.core.common.usecase.ShowSnackbarFailureUseCase
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.CustomDomain
import io.simplelogin.core.model.ui.DialogPayload
import io.simplelogin.core.model.ui.ObjectDialogPayload
import io.simplelogin.feature.auth.usecase.LogOutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(
    @AppVersion val appVersion: String,
    @ApplicationContext private val context: Context,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val showSnackbarFailure: ShowSnackbarFailureUseCase,
) : ViewModel() {

    private val _navBackStack =
        MutableStateFlow(mutableStateListOf<NavKey>(InitializationDestination))
    val navBackStack = _navBackStack.asStateFlow()

    private val _createdAlias = Channel<Alias>(Channel.BUFFERED)
    val createdAlias = _createdAlias.receiveAsFlow()

    var showLogOutDialog = MutableStateFlow(false)

    var showDeviceSettingsDialog = MutableStateFlow(false)

    var accountSettingsDialogPayload = MutableStateFlow<DialogPayload?>(null)

    var mailboxesDialogPayload = MutableStateFlow<DialogPayload?>(null)

    var customDomainsDialogPayload = MutableStateFlow<DialogPayload?>(null)

    var customDomainDetailsDialogPayload =
        MutableStateFlow<ObjectDialogPayload<CustomDomain>?>(null)
    var customDomainDeletedAliasesDialogPayload =
        MutableStateFlow<ObjectDialogPayload<CustomDomain>?>(null)

    val stateFlow: StateFlow<AppRootState> = observeSessionSettings()
        .map {
            AppRootState(
                isReady = true,
                apiKey = it.apiKey
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppRootState.Default
        )

    //region Setup
    init {
        viewModelScope.launch {
            stateFlow
                .collect {
                    _navBackStack.value.apply {
                        clear()
                        if (!it.isReady) {
                            add(InitializationDestination)
                            return@collect
                        }

                        it.apiKey?.let { apiKey ->
                            add(HomeDestination(apiKey.value))
                        } ?: add(LogInDestination)
                    }
                }
        }
    }
    //endregion

    fun goBack() {
        _navBackStack.value.apply { removeAt(lastIndex) }
    }

    //region Drawer
    fun showLogOutDialog() {
        showLogOutDialog.value = true
    }

    fun dismissLogOutDialog() {
        showLogOutDialog.value = false
    }

    fun logOut() {
        showLogOutDialog.value = false
        viewModelScope.launch {
            logOutUseCase()
        }
    }


    fun showDeviceSettingsScreen(asDialog: Boolean) {
        if (asDialog) {
            showDeviceSettingsDialog.value = true
        } else {
            _navBackStack.value.apply {
                add(DeviceSettingsDestination)
            }
        }
    }

    fun dismissDeviceSettingsDialog() {
        showDeviceSettingsDialog.value = false
    }

    fun showAccountSettingsScreen(asDialog: Boolean) {
        withApiKey { apiKey ->
            if (asDialog) {
                accountSettingsDialogPayload.value = DialogPayload(apiKey)
            } else {
                _navBackStack.value.apply {
                    add(AccountSettingsDestination(apiKey.value))
                }
            }
        }
    }

    fun dismissAccountSettingsDialog() {
        accountSettingsDialogPayload.value = null
    }

    fun showMailboxesScreen(asDialog: Boolean) {
        withApiKey { apiKey ->
            if (asDialog) {
                mailboxesDialogPayload.value = DialogPayload(apiKey)
            } else {
                _navBackStack.value.apply {
                    add(MailboxesDestination(apiKey.value))
                }
            }
        }
    }

    fun dismissMailboxesDialog() {
        mailboxesDialogPayload.value = null
    }

    fun showCustomDomainsScreen(asDialog: Boolean) {
        withApiKey { apiKey ->
            if (asDialog) {
                customDomainsDialogPayload.value = DialogPayload(apiKey)
            } else {
                _navBackStack.value.apply {
                    add(CustomDomainsDestination(apiKey.value))
                }
            }
        }
    }

    fun dismissCustomDomainsDialog() {
        customDomainsDialogPayload.value = null
    }

    fun showCustomDomainDetails(domain: CustomDomain, asDialog: Boolean) {
        withApiKey { apiKey ->
            if (asDialog) {
                customDomainDetailsDialogPayload.value =
                    ObjectDialogPayload(apiKey = apiKey, value = domain)
            } else {
                _navBackStack.value.apply {
                    add(CustomDomainDetailsDestination(domain = domain, apiKey = apiKey.value))
                }
            }
        }
    }

    fun dismissCustomDomainDetailsDialog() {
        customDomainDetailsDialogPayload.value = null
    }

    fun showCustomDomainDeletedAliases(domain: CustomDomain, asDialog: Boolean) {
        withApiKey { apiKey ->
            if (asDialog) {
                customDomainDeletedAliasesDialogPayload.value =
                    ObjectDialogPayload(apiKey = apiKey, value = domain)
            } else {
                _navBackStack.value.apply {
                    add(
                        CustomDomainDeletedAliasesDestination(
                            domain = domain,
                            apiKey = apiKey.value
                        )
                    )
                }
            }
        }
    }

    fun dismissCustomDomainDeletedAliasesDialog() {
        customDomainDeletedAliasesDialogPayload.value = null
    }

    //endregion

    //region Home
    fun showCreateAliasScreen() {
        withApiKey { apiKey ->
            _navBackStack.value.apply {
                add(CreateAliasDestination(apiKey.value))
            }
        }
    }

    fun handleCreatedAlias(alias: Alias) {
        _createdAlias.trySend(alias)
        goBack()
    }

    fun removeAliasDetails() {
        _navBackStack.value.apply {
            removeIf { it is AliasDetailsDestination }
        }
    }

    fun viewAliasDetails(alias: Alias) {
        withApiKey { apiKey ->
            _navBackStack.value.apply {
                // When viewing details of an alias, we remove all previous details
                // from navigation stack
                removeIf { it is AliasDetailsDestination }
                removeIf { it is AliasContactsDestination }
                add(AliasDetailsDestination(alias = alias, apiKey = apiKey.value))
            }
        }
    }

    fun viewAliasContacts(alias: Alias) {
        withApiKey { apiKey ->
            uniquelyAddDestination(AliasContactsDestination(alias = alias, apiKey = apiKey.value))
        }
    }

    fun viewAliasActivities(alias: Alias) {
        withApiKey { apiKey ->
            uniquelyAddDestination(AliasActivitiesDestination(alias = alias, apiKey = apiKey.value))
        }
    }
    //endregion

    private fun uniquelyAddDestination(navKey: NavKey) {
        _navBackStack.value.apply {
            if (lastOrNull() != navKey) {
                add(navKey)
            }
        }
    }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: (ApiKey) -> Unit
    ) {
        scope.launch {
            observeSessionSettings().collect { settings ->
                settings.apiKey?.let {
                    block(it)
                } ?: scope.launch {
                    showSnackbarFailure(context.getString(R.string.missing_api_key))
                }
            }
        }
    }
}
