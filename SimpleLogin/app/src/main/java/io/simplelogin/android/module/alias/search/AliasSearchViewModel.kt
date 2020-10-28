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
        SLApiService.fetchAliases(apiKey, _currentPage + 1, _term) { result ->
            _isFetching = false

            result.onSuccess { newAliases ->
                if (newAliases.isEmpty()) {
                    moreToLoad = false
                    _eventUpdateResults.postValue(true)
                } else {
                    _currentPage += 1
                    _aliases.addAll(newAliases)
                    _eventUpdateResults.postValue(true)
                }
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Delete
    private var _deletedAliasIds = mutableListOf<Int>()
    val deletedAliasIds: List<Int>
        get() = _deletedAliasIds

    fun deleteAlias(alias: Alias) {
        SLApiService.deleteAlias(apiKey, alias) { result ->
            result.onSuccess {
                _deletedAliasIds.add(alias.id)
                _aliases.removeAll { it.id == alias.id }
                _eventUpdateResults.postValue(true)
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Toggle
    private var _toggledAliases = mutableListOf<Alias>()
    val toggledAliases: List<Alias>
        get() = _toggledAliases

    private var _toggledAliasIndex = MutableLiveData<Int>()
    val toggledAliasIndex: LiveData<Int>
        get() = _toggledAliasIndex

    fun onHandleToggleAliasComplete() {
        _toggledAliasIndex.value = null
    }

    fun toggleAlias(alias: Alias, index: Int) {
        SLApiService.toggleAlias(apiKey, alias) { result ->
            result.onSuccess { enabled ->
                _aliases.find { it.id == alias.id }?.let { toggledAlias ->
                    toggledAlias.setEnabled(enabled.value)
                    if (!_toggledAliases.contains(toggledAlias)) {
                        _toggledAliases.add(toggledAlias)
                    }
                }

                _toggledAliasIndex.postValue(index)
            }

            result.onFailure {  _error.postValue(it as SLError) }
        }
    }
}
