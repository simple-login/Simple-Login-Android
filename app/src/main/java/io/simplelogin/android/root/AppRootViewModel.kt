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
import io.simplelogin.feature.auth.usecase.LogOutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    private val _dialogStack = MutableStateFlow<List<AppRootDialog>>(emptyList())
    val dialogStack = _dialogStack.asStateFlow()

    val stateFlow: StateFlow<AppRootState> = observeSessionSettings().map {
        AppRootState(isReady = true, apiKey = it.apiKey)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppRootState.Default
    )

    //region Setup
    init {
        viewModelScope.launch {
            stateFlow.collect {
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
        _dialogStack.value = listOf(AppRootDialog.LogOut)
    }

    fun dismissActiveDialog() {
        _dialogStack.value = _dialogStack.value.dropLast(1)
    }

    fun logOut() {
        _dialogStack.value = emptyList()
        viewModelScope.launch {
            logOutUseCase()
        }
    }

    fun showDeviceSettingsScreen() {
        _navBackStack.value.apply {
            add(DeviceSettingsDestination)
        }
    }

    fun showAccountSettingsScreen() {
        addToBackStackWithApiKey { apiKey ->
            AccountSettingsDestination(apiKey.value)
        }
    }

    fun showMailboxesScreen() {
        addToBackStackWithApiKey { apiKey ->
            MailboxesDestination(apiKey.value)
        }
    }

    fun showCustomDomainsScreen() {
        addToBackStackWithApiKey { apiKey ->
            CustomDomainsDestination(apiKey.value)
        }
    }

    fun showCustomDomainDetails(domain: CustomDomain) {
        addToBackStackWithApiKey { apiKey ->
            CustomDomainDetailsDestination(domain = domain, apiKey = apiKey.value)
        }
    }

    fun showCustomDomainDeletedAliases(domain: CustomDomain) {
        addToBackStackWithApiKey { apiKey ->
            CustomDomainDeletedAliasesDestination(domain = domain, apiKey = apiKey.value)
        }
    }
    //endregion

    //region Home
    fun showCreateAliasScreen() {
        addToBackStackWithApiKey { apiKey ->
            CreateAliasDestination(apiKey.value)
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

    private fun addToBackStackWithApiKey(block: (ApiKey) -> NavKey) {
        withApiKey {
            val destination = block(it)
            _navBackStack.value.apply {
                add(destination)
            }
        }
    }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: (ApiKey) -> Unit
    ) {
        scope.launch {
            val settings = observeSessionSettings().first()
            settings.apiKey?.let {
                block(it)
            } ?: showSnackbarFailure(context.getString(R.string.missing_api_key))
        }
    }
}
