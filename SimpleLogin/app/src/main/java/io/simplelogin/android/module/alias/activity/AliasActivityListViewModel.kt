package io.simplelogin.android.module.alias.activity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.AliasActivity

class AliasActivityListViewModel(context: Context, private val alias: Alias) : BaseViewModel(context) {
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

    fun fetchActivities() {
        if (!moreToLoad || _isFetching) return
        _isFetching = true
        SLApiService.fetchAliasActivities(apiKey, alias, _currentPage + 1) { newActivities, error ->
            _isFetching = false

            if (error != null) {
                _error.postValue(error)
            } else if (newActivities != null) {
                if (newActivities.isEmpty()) {
                    moreToLoad = false
                    _eventHaveNewActivities.postValue(false)
                } else {
                    _currentPage += 1
                    _activities.addAll(newActivities)
                    _eventHaveNewActivities.postValue(true)
                }
            }
        }
    }

    fun refreshActivities() {
        _currentPage = -1
        moreToLoad = true
        _activities = mutableListOf()
        fetchActivities()
    }
}