package io.simplelogin.android.module.mailbox

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Mailbox
import java.lang.IllegalStateException

class MailboxListViewModel(private val context: Context) : ViewModel() {
    val apiKey: String by lazy {
        SLSharedPreferences.getApiKey(context) ?: throw IllegalStateException("API key is null")
    }

    // Error
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    // Mailbox list
    private var _mailboxes = mutableListOf<Mailbox>()
    val mailboxes: List<Mailbox>
        get() = _mailboxes

    private val _eventUpdateMailboxes = MutableLiveData<Boolean>()
    val eventUpdateMailboxes: LiveData<Boolean>
        get() = _eventUpdateMailboxes

    fun onHandleUpdateMailboxesComplete() {
        _eventUpdateMailboxes.value = false
    }

    fun fetchMailboxes() {
        SLApiService.fetchMailboxes(apiKey) { result ->
            result.onSuccess { mailboxes ->
                with(_mailboxes) {
                    clear()
                    addAll(mailboxes)
                }
                _eventUpdateMailboxes.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Create mailbox
    private var _createdMailbox = MutableLiveData<String>()
    val createdMailbox: LiveData<String>
        get() = _createdMailbox

    fun onHandleCreatedMailboxComplete() {
        _createdMailbox.value = null
    }

    fun create(email: String) {
        SLApiService.createMailbox(apiKey, email) { result ->
            result.onSuccess { _createdMailbox.postValue(email) }
            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Delete mailbox
    fun deleteMailbox(mailbox: Mailbox) {
        SLApiService.deleteMailbox(apiKey, mailbox) { result ->
            result.onSuccess {
                _mailboxes.removeAll { it.id == mailbox.id }
                _eventUpdateMailboxes.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Make default
    fun makeDefault(mailbox: Mailbox) {
        SLApiService.makeDefaultMailbox(apiKey, mailbox) { result ->
            result.onSuccess { fetchMailboxes() }
            result.onFailure { _error.postValue(it as SLError) }
        }
    }
}
