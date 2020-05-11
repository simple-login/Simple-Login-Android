package io.simplelogin.android.module.alias.activity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.AliasActivity

class AliasActivityListViewModel(context: Context, var alias: Alias) : BaseViewModel(context) {
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

    fun fetchActivities(firebaseAnalytics: FirebaseAnalytics) {
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

    fun refreshActivities(firebaseAnalytics: FirebaseAnalytics) {
        _currentPage = -1
        moreToLoad = true
        _activities = mutableListOf()
        fetchActivities(firebaseAnalytics)
    }

    // Note
    private val _eventNoteUpdate = MutableLiveData<Boolean>()
    val eventNoteUpdate: LiveData<Boolean>
        get() = _eventNoteUpdate

    fun onHandleNoteUpdateComplete() {
        _eventNoteUpdate.value = false
    }

    private var _isUpdatingNote: Boolean = false

    fun updateNote(note: String?, firebaseAnalytics: FirebaseAnalytics) {
        if (_isUpdatingNote) return
        _isUpdatingNote = true
        SLApiService.updateAliasNote(apiKey, alias, note) { result ->
            _isUpdatingNote = false

            result.onSuccess {
                alias.setNote(note)
                _eventNoteUpdate.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }
}