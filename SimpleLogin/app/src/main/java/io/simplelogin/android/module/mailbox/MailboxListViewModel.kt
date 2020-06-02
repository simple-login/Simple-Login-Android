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


    fun fetchMailboxes() {
        SLApiService.fetchMailboxes(apiKey) { result ->
            result.onSuccess { mailboxes ->
                _mailboxes.clear()
                _mailboxes.addAll(mailboxes)
                _eventUpdateMailboxes.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }
}