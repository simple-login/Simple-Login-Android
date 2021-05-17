package io.simplelogin.android.module.alias.create

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.AliasMailbox
import io.simplelogin.android.utils.model.Mailbox
import io.simplelogin.android.utils.model.UserOptions

class AliasCreateViewModel(context: Context) : BaseViewModel(context) {
    private var _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    private var _userOptions = MutableLiveData<UserOptions>()
    val userOptions: LiveData<UserOptions>
        get() = _userOptions

    var mailboxes: List<Mailbox> = emptyList()
        private set

    private var _selectedMailboxes = MutableLiveData<List<AliasMailbox>>()
    val selectedMailboxes: LiveData<List<AliasMailbox>>
        get() = _selectedMailboxes

    init {
        _error.value = null
        _userOptions.value = null
        _selectedMailboxes.value = null
    }

    fun fetchUserOptionsAndMailboxes() {
        SLApiService.fetchUserOptions(apiKey) { userOptionsResult ->
            userOptionsResult.onFailure { _error.postValue(it as SLError) }

            userOptionsResult.onSuccess { fetchedUserOptions ->
                SLApiService.fetchMailboxes(apiKey) { mailboxesResult ->
                    mailboxesResult.onFailure { _error.postValue(it as SLError) }

                    mailboxesResult.onSuccess { fetchedMailboxes ->
                        _userOptions.postValue(fetchedUserOptions)
                        mailboxes = fetchedMailboxes.filter { it.isVerified }

                        val defaultMailbox = fetchedMailboxes.first { it.isDefault }
                        _selectedMailboxes.postValue(listOf(defaultMailbox.toAliasMailbox()))
                    }
                }
            }
        }
    }

    fun setSelectedMailboxes(selectedMailboxes: List<AliasMailbox>) {
        _selectedMailboxes.value = selectedMailboxes
    }
}
