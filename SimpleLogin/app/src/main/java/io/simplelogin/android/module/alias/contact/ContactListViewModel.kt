package io.simplelogin.android.module.alias.contact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.Contact

class ContactListViewModel(context: Context, private val alias: Alias) : BaseViewModel(context) {
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    private var _contacts = mutableListOf<Contact>()
    val contacts: List<Contact>
        get() = _contacts

    private var _currentPage = -1
    var moreToLoad: Boolean = true
        private set
    private var _isFetching: Boolean = false

    private val _eventHaveNewContacts = MutableLiveData<Boolean>()
    val eventHaveNewContacts: LiveData<Boolean>
        get() = _eventHaveNewContacts

    fun fetchContacts() {
        if (!moreToLoad || _isFetching) return
        _isFetching = true
        SLApiService.fetchContacts(apiKey, alias, _currentPage + 1) { result ->
            _isFetching = false

            result.onSuccess { newContacts ->
                if (newContacts.isEmpty()) {
                    moreToLoad = false
                    _eventHaveNewContacts.postValue(false)
                } else {
                    _currentPage += 1
                    _contacts.addAll(newContacts)
                    _eventHaveNewContacts.postValue(true)
                }
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    fun refreshContacts() {
        _currentPage = -1
        moreToLoad = true
        _contacts = mutableListOf()
        fetchContacts()
    }

    // Create
    private var _eventFinishCallingCreateContact = MutableLiveData<Boolean>()
    val eventFinishCallingCreateContact: LiveData<Boolean>
        get() = _eventFinishCallingCreateContact

    fun onHandleFinishCallingCreateContactComplete() {
        _eventFinishCallingCreateContact.value = false
    }

    private var _eventCreatedContact = MutableLiveData<String>()
    val eventCreatedContact: LiveData<String>
        get() = _eventCreatedContact

    fun onHandleCreatedContactComplete() {
        _eventCreatedContact.value = null
    }

    fun create(email: String) {
        SLApiService.createContact(apiKey, alias, email) { result ->
            _eventFinishCallingCreateContact.postValue(true)
            result.onSuccess { contact ->
                if (contact.existed) {
                    _error.postValue(SLError.DuplicatedContact)
                } else {
                    _eventCreatedContact.postValue(email)
                }
            }
            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Delete
    private var _eventFinishCallingDeleteContact = MutableLiveData<Boolean>()
    val eventFinishCallingDeleteContact: LiveData<Boolean>
        get() = _eventFinishCallingDeleteContact

    fun onHandleFinishCallingDeleteContactComplete() {
        _eventFinishCallingDeleteContact.value = false
    }

    private var _eventDeletedContact = MutableLiveData<String>()
    val eventDeletedContact: LiveData<String>
        get() = _eventDeletedContact

    fun onHandleDeletedContactComplete() {
        _eventDeletedContact.value = null
    }

    fun delete(contact: Contact) {
        SLApiService.deleteContact(apiKey, contact) { result ->
            _eventFinishCallingDeleteContact.postValue(true)
            result.onSuccess { _eventDeletedContact.postValue(contact.email) }
            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Toggle
    private var _eventFinishTogglingContact = MutableLiveData<Boolean>()
    val eventFinishTogglingContact: LiveData<Boolean>
        get() = _eventFinishTogglingContact

    fun onHandleToggledContactComplete() {
        _eventFinishTogglingContact.value = false
    }

    fun toggle(contact: Contact) {
        SLApiService.toggleContact(apiKey, contact) { result ->
            _eventFinishTogglingContact.postValue(true)
            result.onSuccess { contactToggleResult ->
                _contacts.find { it.id == contact.id }?.blockForward = contactToggleResult.blockForward
            }
            result.onFailure { _error.postValue(it as SLError) }
        }
    }
}
