package io.simplelogin.feature.mailboxes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.Mailbox
import io.simplelogin.core.model.api.UpdateMailboxOption
import io.simplelogin.core.network.datasource.MailboxesRemoteDatasource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MailboxesViewModel.Factory::class)
internal class MailboxesViewModel @AssistedInject constructor(
    @Assisted private val apiKeyValue: String,
    private val datasource: MailboxesRemoteDatasource
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(apiKeyValue: String): MailboxesViewModel
    }

    private val _stateFlow = MutableStateFlow(MailboxesState.Default)
    val stateFlow: StateFlow<MailboxesState> = _stateFlow

    init {
        fetchMailboxes()
    }

    fun fetchMailboxes() {
        _stateFlow.update { it.copy(isFetching = true) }
        withApiKey { apiKey ->
            datasource.getMailboxes(apiKey)
                .fold(onSuccess = { result ->
                    _stateFlow.update { it ->
                        val sortedMailboxes = result.value.sortedWith(
                            compareByDescending { it.creationTimestamp }
                        )
                        it.copy(
                            mailboxes = sortedMailboxes,
                            isFetching = false
                        )
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(isFetching = false, fetchError = error)
                    }
                })
        }
    }

    fun add(email: String) {
        _stateFlow.update { it.copy(isUpdating = true) }
        withApiKey { apiKey ->
            datasource.createMailbox(apiKey = apiKey, email = email)
                .fold(onSuccess = { result ->
                    _stateFlow.update {
                        val updatedMailboxes = listOf(result) + (it.mailboxes ?: emptyList())
                        it.copy(
                            mailboxes = updatedMailboxes,
                            isUpdating = false,
                            addedMailbox = result
                        )
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(isUpdating = false, updateError = error)
                    }
                })
        }
    }

    fun setAsDefault(newDefaultMailbox: Mailbox) {
        _stateFlow.update { it.copy(isUpdating = true) }
        withApiKey { apiKey ->
            datasource.updateMailbox(
                apiKey = apiKey,
                mailbox = newDefaultMailbox,
                options = UpdateMailboxOption.Default(true)
            ).fold(onSuccess = {
                val updatedMailboxes = _stateFlow.value.mailboxes?.map { mailbox ->
                    when {
                        mailbox.id == newDefaultMailbox.id -> mailbox.copy(default = true)
                        mailbox.default -> mailbox.copy(default = false)
                        else -> mailbox
                    }
                }
                _stateFlow.update {
                    it.copy(
                        mailboxes = updatedMailboxes,
                        newDefaultMailbox = newDefaultMailbox,
                        isUpdating = false
                    )
                }
            }, onFailure = { error ->
                _stateFlow.update {
                    it.copy(isUpdating = false, updateError = error)
                }
            })
        }
    }

    fun deleteMailbox(mailbox: Mailbox, option: MailboxDeleteOption) {
        _stateFlow.update { it.copy(isUpdating = true) }
        withApiKey { apiKey ->
            datasource.deleteMailbox(
                apiKey = apiKey,
                mailbox = mailbox,
                transferredMailbox = option.mailbox
            ).fold(onSuccess = {
                _stateFlow.update { state ->
                    state.copy(
                        mailboxes = state.mailboxes?.filter { it.id != mailbox.id },
                        isUpdating = false,
                        deletedMailbox = mailbox
                    )
                }
            }, onFailure = { error ->
                _stateFlow.update {
                    it.copy(isUpdating = false, updateError = error)
                }
            })
        }
    }

    fun clearUpdateError() = _stateFlow.update { it.copy(updateError = null) }

    fun clearAddedMailbox() = _stateFlow.update { it.copy(addedMailbox = null) }

    fun clearDeletedMailbox() = _stateFlow.update { it.copy(deletedMailbox = null) }

    fun clearNewDefaultMailbox() = _stateFlow.update { it.copy(newDefaultMailbox = null) }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.(ApiKey) -> Unit
    ) = scope.launch { block(ApiKey(apiKeyValue)) }
}