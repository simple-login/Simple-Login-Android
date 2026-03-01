package io.simplelogin.android.ui.home.aliasdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.MailboxesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.updateMailboxes
import io.simplelogin.android.data.remote.datasource.updateName
import io.simplelogin.android.data.remote.datasource.updateNote
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AliasDetailViewModel.Factory::class)
class AliasDetailViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val alias: Alias,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val snackbarManager: SnackbarManager,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val aliasesRemoteDatasource: AliasesRemoteDatasource,
    private val mailboxesRemoteDatasource: MailboxesRemoteDatasource,
    observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(alias: Alias): AliasDetailViewModel
    }

    private val aliasStateFlow = MutableStateFlow(alias)

    private val activitiesStateFlow =
        MutableStateFlow<AliasActivitiesState>(AliasActivitiesState.Loading)

    private val mailboxesToUpdateStateFlow = MutableStateFlow<List<Mailbox>?>(null)

    val stateFlow = combine(
        observeDeviceSettings(),
        aliasStateFlow,
        activitiesStateFlow,
        mailboxesToUpdateStateFlow
    ) { deviceSettings, alias, activities, mailboxes ->
        AliasDetailScreenState(
            alias = alias,
            devicePreferences = deviceSettings,
            activitiesState = activities,
            mailboxesToUpdate = mailboxes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AliasDetailScreenState.Default
    )

    init {
        getActivities()
    }

    fun getActivities() {
        activitiesStateFlow.value = AliasActivitiesState.Loading
        withApiKey { apiKey ->
            aliasesRemoteDatasource.getActivities(
                apiKey = apiKey,
                aliasId = alias.id,
                page = 0
            ).fold(
                onSuccess = { activities ->
                    activitiesStateFlow.value = AliasActivitiesState.Loaded(
                        activities = activities,
                        hasMoreActivities = activities.count() >= 20
                    )
                },
                onFailure = {
                    activitiesStateFlow.value = AliasActivitiesState.Error(it)
                }
            )
        }
    }

    fun updateNote(note: String, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.emit(true)
            aliasesRemoteDatasource.updateNote(
                apiKey = apiKey,
                aliasId = alias.id,
                note = note
            ).fold(onSuccess = {
                loadingState.emit(false)
                aliasStateFlow.update { it.copy(note = note) }
                val message = context.getString(R.string.note_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                onSuccess(aliasStateFlow.value)
            }, onFailure = ::handle)
        }
    }

    fun updateName(name: String, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.emit(true)
            aliasesRemoteDatasource.updateName(
                apiKey = apiKey,
                aliasId = alias.id,
                name = name
            ).fold(onSuccess = {
                loadingState.emit(false)
                aliasStateFlow.update { it.copy(name = name) }
                val message = context.getString(R.string.display_name_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                onSuccess(aliasStateFlow.value)
            }, onFailure = ::handle)
        }
    }

    fun getMailboxesToUpdate() {
        withApiKey { apiKey ->
            loadingState.emit(true)
            mailboxesRemoteDatasource.getMailboxes(apiKey)
                .fold(onSuccess = { mailboxes ->
                    loadingState.emit(false)
                    mailboxesToUpdateStateFlow.emit(mailboxes.value)
                }, onFailure = ::handle)
        }
    }

    fun updateMailboxes(mailboxes: List<Mailbox>, onSuccess: (Alias) -> Unit) {
        withApiKey { apiKey ->
            loadingState.emit(true)
            aliasesRemoteDatasource.updateMailboxes(
                apiKey = apiKey,
                aliasId = alias.id,
                mailboxes = mailboxes
            ).fold(onSuccess = {
                loadingState.emit(false)
                aliasStateFlow.update { it.copy(mailboxes = mailboxes.map { it.toMailboxLite() }) }
                val message = context.getString(R.string.mailboxes_updated)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                onSuccess(aliasStateFlow.value)
            }, onFailure = ::handle)
        }
    }

    fun removeMailboxesToUpdate() {
        mailboxesToUpdateStateFlow.value = null
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

    private suspend fun handle(error: ApiError) {
        loadingState.emit(false)
        val config = SnackbarConfiguration(
            message = error.description(context),
            type = SnackbarType.FAILURE
        )
        snackbarManager.showSnackbar(config)
    }
}