package io.simplelogin.android.ui.home.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.remote.datasource.MailboxesRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MailboxesViewModel @Inject constructor(
    private val datasource: MailboxesRemoteDatasource,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    private var apiKey: ApiKey? = null
    private val _stateFlow = MutableStateFlow(MailboxesState.Default)
    val stateFlow: StateFlow<MailboxesState> = _stateFlow

    init {
        fetchMailboxes()
    }

    fun fetchMailboxes() {
        _stateFlow.update { it.copy(isFetching = true) }
        withApiKey { apiKey ->
            when (val result = datasource.getMailboxes(apiKey)) {
                is Result.Success -> _stateFlow.update {
                    it.copy(
                        mailboxes = result.value.value,
                        isFetching = false
                    )
                }

                is Result.Failure -> _stateFlow.update {
                    it.copy(
                        isFetching = false,
                        fetchError = result.error
                    )
                }
            }
        }
    }

    fun deleteMailbox(mailbox: Mailbox) {}

    fun clearUpdateError() {
        _stateFlow.update {
            it.copy(updateError = null)
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
}