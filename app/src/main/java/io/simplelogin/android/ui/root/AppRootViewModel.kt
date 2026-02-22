package io.simplelogin.android.ui.root

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.data.models.preferences.LockTimeOut
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
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
    observeSessionSettingsUseCase: ObserveSessionSettingsUseCase,
    private val updateSessionSettingsUseCase: UpdateSessionSettingsUseCase
) : ViewModel() {

    private val _navBackStack =
        MutableStateFlow(mutableStateListOf<NavKey>(InitializationDestination))
    val navBackStack = _navBackStack.asStateFlow()

    private val _createdAlias = Channel<Alias>(Channel.BUFFERED)
    val createdAlias = _createdAlias.receiveAsFlow()

    var showLogOutDialog = MutableStateFlow(false)

    var showDeviceSettingsDialog = MutableStateFlow(false)

    var showAccountSettingsDialog = MutableStateFlow(false)

    var showMailboxesDialog = MutableStateFlow(false)

    var showCustomDomainsDialog = MutableStateFlow(false)

    var customDomainDetailsAsDialog = MutableStateFlow<CustomDomain?>(null)
    var customDomainDeletedAliasesAsDialog = MutableStateFlow<CustomDomain?>(null)

    val stateFlow: StateFlow<AppRootState> = observeSessionSettingsUseCase()
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

                        if (it.apiKey != null) {
                            add(HomeDestination)
                        } else {
                            add(LogInDestination)
                        }
                    }
                }
        }
    }
    //endregion

    fun goBack() {
        _navBackStack.value.apply { removeAt(lastIndex) }
    }

    //region Log in/sign up
    fun updateApiKey(apiKey: ApiKey?) {
        viewModelScope.launch {
            updateSessionSettingsUseCase.invoke {
                if (apiKey == null) {
                    // Log out, remove API key and reset lock settings
                    it.copy(
                        apiKey = null,
                        lockEnabled = false,
                        lockTimeOut = LockTimeOut.DEFAULT
                    )
                } else {
                    // Login
                    it.copy(apiKey = apiKey)
                }
            }
        }
    }
    //endregion

    //region Drawer
    fun showLogOutDialog() {
        showLogOutDialog.value = true
    }

    fun dismissLogOutDialog() {
        showLogOutDialog.value = false
    }

    fun logOut() {
        showLogOutDialog.value = false
        updateApiKey(null)
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
        if (asDialog) {
            showAccountSettingsDialog.value = true
        } else {
            _navBackStack.value.apply {
                add(AccountSettingsDestination)
            }
        }
    }

    fun dismissAccountSettingsDialog() {
        showAccountSettingsDialog.value = false
    }

    fun showMailboxesScreen(asDialog: Boolean) {
        if (asDialog) {
            showMailboxesDialog.value = true
        } else {
            _navBackStack.value.apply {
                add(MailboxesDestination)
            }
        }
    }

    fun dismissMailboxesDialog() {
        showMailboxesDialog.value = false
    }

    fun showCustomDomainsScreen(asDialog: Boolean) {
        if (asDialog) {
            showCustomDomainsDialog.value = true
        } else {
            _navBackStack.value.apply {
                add(CustomDomainsDestination)
            }
        }
    }

    fun dismissCustomDomainsDialog() {
        showCustomDomainsDialog.value = false
    }

    fun showCustomDomainDetails(domain: CustomDomain, asDialog: Boolean) {
        if (asDialog) {
            customDomainDetailsAsDialog.value = domain
        } else {
            _navBackStack.value.apply {
                add(CustomDomainDetailsDestination(domain))
            }
        }
    }

    fun dismissCustomDomainDetailsDialog() {
        customDomainDetailsAsDialog.value = null
    }

    fun showCustomDomainDeletedAliases(domain: CustomDomain, asDialog: Boolean) {
        if (asDialog) {
            customDomainDeletedAliasesAsDialog.value = domain
        } else {
            _navBackStack.value.apply {
                add(CustomDomainDeletedAliasesDestination(domain))
            }
        }
    }

    fun dismissCustomDomainDeletedAliasesDialog() {
        customDomainDeletedAliasesAsDialog.value = null
    }

    //endregion

    //region Home
    fun showCreateAliasScreen() {
        _navBackStack.value.apply {
            add(CreateAliasDestination)
        }
    }

    fun handleCreatedAlias(alias: Alias) {
        _createdAlias.trySend(alias)
        goBack()
    }

    fun viewAliasDetails(alias: Alias) {
        _navBackStack.value.apply {
            // When viewing details of an alias, we remove all previous details
            // from navigation stack
            removeIf { it is AliasDetailsDestination }
            removeIf { it is AliasContactsDestination }
            add(AliasDetailsDestination(alias))
        }
    }

    fun viewAliasContacts(alias: Alias) {
        _navBackStack.value.apply {
            if (lastOrNull() != AliasContactsDestination(alias)) {
                add(AliasContactsDestination(alias))
            }
        }
    }
    //endregion
}
