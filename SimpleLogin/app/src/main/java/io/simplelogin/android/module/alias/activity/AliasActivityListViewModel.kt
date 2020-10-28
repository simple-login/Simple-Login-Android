package io.simplelogin.android.module.alias.activity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.AliasActivity
import io.simplelogin.android.utils.model.AliasMailbox

class AliasActivityListViewModel(private val context: Context, var alias: Alias) : BaseViewModel(context) {
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    private var _activities = mutableListOf<AliasActivity>()
    val activities: List<AliasActivity>
        get() = _activities

    private var _currentPage = -1
    var moreToLoad: Boolean = true
        private set
    private var _isFetching: Boolean = false

    private val _eventHaveNewActivities = MutableLiveData<Boolean>()
    val eventHaveNewActivities: LiveData<Boolean>
        get() = _eventHaveNewActivities

    fun onHandleHaveNewActivitiesComplete() {
        _eventHaveNewActivities.value = false
    }

    fun fetchActivities() {
        if (!moreToLoad || _isFetching) return
        _isFetching = true
        SLApiService.fetchAliasActivities(apiKey, alias, _currentPage + 1) { result ->
            _isFetching = false

            result.onSuccess { newActivities ->
                if (newActivities.isEmpty()) {
                    moreToLoad = false
                    _eventHaveNewActivities.postValue(false)
                } else {
                    _currentPage += 1
                    _activities.addAll(newActivities)
                    _eventHaveNewActivities.postValue(true)
                }
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    fun refreshActivities() {
        _currentPage = -1
        moreToLoad = true
        _activities = mutableListOf()
        fetchActivities()
    }

    // Metadata: mailboxes, name & note
    private val _eventUpdateMetadata = MutableLiveData<Boolean>()
    val eventUpdateMetadata: LiveData<Boolean>
        get() = _eventUpdateMetadata

    fun onHandleUpdateMetadataComplete() {
        _eventUpdateMetadata.value = false
    }

    // Mailboxes
    private var _isUpdatingMailboxes: Boolean = false
    fun updateMailboxes(mailboxes: List<AliasMailbox>) {
        if (_isUpdatingMailboxes) return
        _isUpdatingMailboxes = true
        SLApiService.updateAliasMailboxes(apiKey, alias, mailboxes) { result ->
            _isUpdatingMailboxes = false

            result.onSuccess {
                alias.setMailboxes(context, mailboxes)
                _eventUpdateMetadata.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Name
    private var _isUpdatingName: Boolean = false
    fun updateName(name: String?) {
        if (_isUpdatingName) return
        _isUpdatingName = true
        val nullableName = if (name == "") null else name
        SLApiService.updateAliasName(apiKey, alias, nullableName) { result ->
            _isUpdatingName = false

            result.onSuccess {
                alias.setName(nullableName)
                _eventUpdateMetadata.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Note
    private var _isUpdatingNote: Boolean = false

    fun updateNote(note: String?) {
        if (_isUpdatingNote) return
        _isUpdatingNote = true
        val nullableNote = if (note == "") null else note
        SLApiService.updateAliasNote(apiKey, alias, nullableNote) { result ->
            _isUpdatingNote = false

            result.onSuccess {
                alias.setNote(nullableNote)
                _eventUpdateMetadata.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }
}
