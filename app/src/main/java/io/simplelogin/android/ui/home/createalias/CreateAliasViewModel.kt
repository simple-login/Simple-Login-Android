package io.simplelogin.android.ui.home.createalias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.models.Result
import io.simplelogin.android.models.api.AliasOptions
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.Mailbox
import io.simplelogin.android.models.api.Mailboxes
import io.simplelogin.android.models.api.Suffix
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.simplelogin.android.data.network.CreateAliasBody

@HiltViewModel(assistedFactory = CreateAliasViewModel.Factory::class)
class CreateAliasViewModel @AssistedInject constructor(
    @Assisted private val apiKeyValue: String,
    private val datasource: CreationRemoteDatasource,
    private val observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(apiKeyValue: String): CreateAliasViewModel
    }

    private val _stateFlow = MutableStateFlow(CreateAliasState.Default)
    val stateFlow: StateFlow<CreateAliasState> = _stateFlow.asStateFlow()

    init {
        fetchOptions()
    }

    fun fetchOptions() {
        _stateFlow.update { CreateAliasState.Default }
        withApiKey { apiKey ->
            val mailboxes = async(Dispatchers.IO) { datasource.getMailboxes(apiKey = apiKey) }
            val options = async(Dispatchers.IO) { datasource.getAliasOptions(apiKey = apiKey) }
            handleResults(
                mailboxesResult = mailboxes.await(),
                optionsResult = options.await()
            )
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

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.(ApiKey) -> Unit
    ) = scope.launch {
        block(ApiKey(apiKeyValue))
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

    fun dismissCreateError() {
        _stateFlow.update { it.copy(createError = null) }
    }
}