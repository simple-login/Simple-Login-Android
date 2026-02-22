package io.simplelogin.android.ui.home.createalias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.models.api.Mailboxes
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.data.remote.CreateAliasBody
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CreateAliasViewModel @Inject constructor(
    private val datasource: CreationRemoteDatasource,
    private val observeDeviceSettings: ObserveDeviceSettingsUseCase,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    private var apiKey: ApiKey? = null
    private val _stateFlow = MutableStateFlow(CreateAliasState.Default)
    val stateFlow: StateFlow<CreateAliasState> = _stateFlow.asStateFlow()

    init {
        fetchOptions()
    }

    fun fetchOptions() {
        _stateFlow.update { CreateAliasState.Default }
        withApiKey { apiKey ->
            coroutineScope {
                val mailboxes = async { datasource.getMailboxes(apiKey = apiKey) }
                val options = async { datasource.getAliasOptions(apiKey = apiKey) }
                handleResults(
                    mailboxesResult = mailboxes.await(),
                    optionsResult = options.await()
                )
            }
        }
    }

    private suspend fun handleResults(
        mailboxesResult: Result<Mailboxes, ApiError>,
        optionsResult: Result<AliasOptions, ApiError>
    ) {
        when {
            mailboxesResult is Result.Success && optionsResult is Result.Success -> {
                val devicePreferences = observeDeviceSettings().first()
                val sortedSuffixes = optionsResult.value.suffixes.sortedWith(
                    compareByDescending<Suffix> { it.isCustom }
                        .thenByDescending { it.isPremium }
                )
                val sortedMailboxes = mailboxesResult.value.value.sortedWith(
                    compareByDescending<Mailbox> { it.default }
                        .thenByDescending { it.verified }
                )
                val randomCharacterCount = devicePreferences.prefixRandomCharacterCount
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        defaultPrefix = devicePreferences.defaultPrefix.generate(
                            randomCharacterCount
                        ),
                        randomCharacterCount = randomCharacterCount,
                        aliasOptions = optionsResult.value.copy(suffixes = sortedSuffixes),
                        mailboxes = sortedMailboxes,
                        fetchError = null
                    )
                }
            }

            mailboxesResult is Result.Failure ->
                _stateFlow.update {
                    it.copy(isLoading = false, fetchError = mailboxesResult.error)
                }


            optionsResult is Result.Failure -> _stateFlow.update {
                it.copy(isLoading = false, fetchError = optionsResult.error)
            }
        }
    }

    private fun withApiKey(block: suspend (ApiKey) -> Unit) {
        apiKey?.let { viewModelScope.launch { block(it) } }
            ?: viewModelScope.launch {
                observeSessionSettings()
                    .mapNotNull { it.apiKey }
                    .first()
                    .let { fetchedApiKey ->
                        apiKey = fetchedApiKey
                        block(fetchedApiKey)
                    }
            }
    }

    fun create(body: CreateAliasBody) {
        _stateFlow.update { it.copy(isLoading = true) }
        withApiKey { apiKey ->
            datasource.create(apiKey = apiKey, body = body)
                .fold(onSuccess = { alias ->
                    _stateFlow.update { it.copy(isLoading = false, createdAlias = alias) }
                }, onFailure = { error ->
                    _stateFlow.update { it.copy(isLoading = false, createError = error) }
                })
        }
    }
}