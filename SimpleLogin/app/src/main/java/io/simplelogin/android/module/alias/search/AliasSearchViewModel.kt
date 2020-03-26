package io.simplelogin.android.module.alias.search

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias

class AliasSearchViewModel(context: Context) : BaseViewModel(context) {
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    private var _aliases = mutableListOf<Alias>()
    val aliases: List<Alias>
        get() = _aliases

    private var _currentPage = -1
    var moreToLoad: Boolean = true
        private set
    private var _isFetching: Boolean = false

    private val _eventUpdateResults = MutableLiveData<Boolean>()
    val eventUpdateResults: LiveData<Boolean>
        get() = _eventUpdateResults

    fun onHandleUpdateResultsComplete() {
        _eventUpdateResults.value = false
    }

    fun forceUpdateResults() {
        _eventUpdateResults.value = true
    }

    private var _term: String? = null
    val term: String?
        get() = _term

    fun search(searchTerm: String? = null) {
        searchTerm?.let {
            // When searchTerm is not null -> a new search with different term
            _term = it
            _currentPage = -1
            _aliases = mutableListOf()
            moreToLoad = true
            _isFetching = false
        }

        if (_term == null) {
            _error.value = SLError.SearchTermNull
            return
        }

        if (!moreToLoad || _isFetching) return
        _isFetching = true
        SLApiService.fetchAliases(apiKey, _currentPage + 1, _term) { newAliases, error ->
            _isFetching = false

            if (error != null) {
                _error.postValue(error)
            } else if (newAliases != null) {
                if (newAliases.isEmpty()) {
                    moreToLoad = false
                    _eventUpdateResults.postValue(true)
                } else {
                    _currentPage += 1
                    _aliases.addAll(newAliases)
                    _eventUpdateResults.postValue(true)
                }
            }
        }
    }

    // Toggle
    private var _toggledAliasIds = mutableListOf<Int>()
    val toggledAliasIds: List<Int>
        get() = _toggledAliasIds

    private var _toggledAliasIndex = MutableLiveData<Int>()
    val toggledAliasIndex: LiveData<Int>
        get() = _toggledAliasIndex

    fun onHandleToggleAliasComplete() {
        _toggledAliasIndex.value = null
    }

    fun toggleAlias(alias: Alias, index: Int) {
        SLApiService.toggleAlias(apiKey, alias) { enabled, error ->
            if (error != null) {
                _error.postValue(error)
            } else if (enabled != null) {
                _aliases.find { it.id == alias.id }?.setEnabled(enabled)

                if (!_toggledAliasIds.contains(alias.id)) {
                    _toggledAliasIds.add(alias.id)
                }

                _toggledAliasIndex.postValue(index)
            }
        }
    }

    override fun onCleared() {
        _error.value = null
        _aliases.clear()
        _isFetching = false
        _currentPage = -1
        moreToLoad = true
        _eventUpdateResults.value = false
        _term = null
    }
}