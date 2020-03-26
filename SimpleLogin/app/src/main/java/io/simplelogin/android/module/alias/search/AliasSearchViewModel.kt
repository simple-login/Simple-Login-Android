package io.simplelogin.android.module.alias.search

import android.content.Context
import android.util.Log
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

    private var _term: String? = null

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
        SLApiService.fetchAliases(apiKey,  _currentPage + 1, _term) { newAliases, error ->
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
}