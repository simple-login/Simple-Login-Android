package io.simplelogin.android.ui.home.aliasdetail

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
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.ui.ActivityUiAction
import io.simplelogin.android.data.remote.datasource.AliasDetailsRemoteDatasource
import io.simplelogin.android.data.remote.datasource.MailboxesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.updateMailboxes
import io.simplelogin.android.data.remote.datasource.updateName
import io.simplelogin.android.data.remote.datasource.updateNote
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.ActivityUiActionHandler
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AliasDetailViewModel.Factory::class)
class AliasDetailViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val aliasIdValue: Int,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val snackbarManager: SnackbarManager,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val aliasDetailsRemoteDatasource: AliasDetailsRemoteDatasource,
    private val mailboxesRemoteDatasource: MailboxesRemoteDatasource,
    private val activityUiActionHandler: ActivityUiActionHandler,
    observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(aliasIdValue: Int): AliasDetailViewModel
    }

    private val aliasId = AliasId(aliasIdValue)

    private val _stateFlow =
        MutableStateFlow<AliasDetailScreenState>(AliasDetailScreenState.Loading)
    val stateFlow: StateFlow<AliasDetailScreenState> = _stateFlow

    private val _mailboxesToUpdateStateFlow = MutableStateFlow<List<Mailbox>?>(null)
    val mailboxesToUpdateStateFlow: StateFlow<List<Mailbox>?> = _mailboxesToUpdateStateFlow

    val devicePreferencesStateFlow = observeDeviceSettings().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevicePreferences.Default
    )

    fun refresh() {
        withApiKey { apiKey ->
            _stateFlow.value = AliasDetailScreenState.Loading
            coroutineScope {
                val alias =
                    async {
                        aliasDetailsRemoteDatasource.getAlias(apiKey = apiKey, aliasId = aliasId)
                    }
                val activities =
                    async {
                        aliasDetailsRemoteDatasource.getActivities(
                            apiKey = apiKey,
                            aliasId = aliasId,
                            page = 0
                        )
                    }
                handleResults(aliasResult = alias.await(), activitiesResult = activities.await())
            }
        }
    }

    private fun handleResults(
        aliasResult: Result<Alias, ApiError>,
        activitiesResult: Result<List<AliasActivity>, ApiError>
    ) {
        when {
            aliasResult is Result.Success && activitiesResult is Result.Success ->
                _stateFlow.value = AliasDetailScreenState.Loaded(
                    alias = aliasResult.value,
                    activities = activitiesResult.value,
                    hasMoreActivities = activitiesResult.value.count() >= PAGE_SIZE
                )

            aliasResult is Result.Failure ->
                _stateFlow.value = AliasDetailScreenState.Error(aliasResult.error)

            activitiesResult is Result.Failure ->
                _stateFlow.value = AliasDetailScreenState.Error(activitiesResult.error)
        }
    }

    fun updateNote(note: String, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.value = true
            aliasDetailsRemoteDatasource.updateNote(
                apiKey = apiKey,
                aliasId = aliasId,
                note = note
            ).fold(onSuccess = {
                loadingState.value = false
                updateStateAlias {
                    val updated = it.copy(note = note)
                    onSuccess(updated)
                    updated
                }
                val message = context.getString(R.string.note_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
            }, onFailure = ::handle)
        }
    }

    fun updateName(name: String, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.value = true
            aliasDetailsRemoteDatasource.updateName(
                apiKey = apiKey,
                aliasId = aliasId,
                name = name
            ).fold(onSuccess = {
                loadingState.value = false
                updateStateAlias {
                    val updated = it.copy(name = name)
                    onSuccess(updated)
                    updated
                }
                val message = context.getString(R.string.display_name_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
            }, onFailure = ::handle)
        }
    }

    fun getMailboxesToUpdate() {
        withApiKey { apiKey ->
            loadingState.value = true
            mailboxesRemoteDatasource.getMailboxes(apiKey)
                .fold(onSuccess = { mailboxes ->
                    loadingState.value = false
                    _mailboxesToUpdateStateFlow.value = mailboxes.value
                }, onFailure = ::handle)
        }
    }

    fun updateMailboxes(mailboxes: List<Mailbox>, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.value = true
            aliasDetailsRemoteDatasource.updateMailboxes(
                apiKey = apiKey,
                aliasId = aliasId,
                mailboxes = mailboxes
            ).fold(onSuccess = {
                loadingState.value = false
                val mailboxLites = mailboxes.map { it.toMailboxLite() }
                updateStateAlias {
                    val updated = it.copy(mailboxes = mailboxLites)
                    onSuccess(updated)
                    updated
                }
                val message = context.getString(R.string.mailboxes_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
            }, onFailure = ::handle)
        }
    }

    fun removeMailboxesToUpdate() {
        _mailboxesToUpdateStateFlow.value = null
    }

    fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction) {
        viewModelScope.launch {
            activityUiActionHandler.handleActivityAction(activity = activity, action = action)
        }
    }

    private fun withApiKey(perform: suspend (ApiKey) -> Unit) {
        viewModelScope.launch {
            observeSessionSettings().collect { settings ->
                settings.apiKey?.let {
                    perform(it)
                }
            }
        }
    }

    private fun updateStateAlias(perform: (Alias) -> Alias) {
        val value = _stateFlow.value
        if (value is AliasDetailScreenState.Loaded) {
            val updatedAlias = perform(value.alias)
            _stateFlow.value = value.copy(alias = updatedAlias)
        }
    }

    private suspend fun handle(error: ApiError) {
        loadingState.value = false
        val config = SnackbarConfiguration(
            message = error.description(context),
            type = SnackbarType.FAILURE
        )
        snackbarManager.showSnackbar(config)
    }
}
