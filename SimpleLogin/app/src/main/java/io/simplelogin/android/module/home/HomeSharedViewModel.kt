package io.simplelogin.android.module.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import java.lang.IllegalStateException

class HomeSharedViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey: String by lazy {
        SLSharedPreferences.getApiKey(application) ?: throw IllegalStateException("API key is null")
    }
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    // Aliases
    private var currentPage = -1
    private var _moreAliasesToLoad: Boolean = true
    val moreAliasesToLoad: Boolean
        get() = _moreAliasesToLoad

    var aliases = mutableListOf<Alias>()
        private set

    private var _isFetchingAliases = false

    private val _eventUpdateAliases = MutableLiveData<Boolean>()
    val eventUpdateAliases: LiveData<Boolean>
        get() = _eventUpdateAliases

    init {
        _error.value = null
        _eventUpdateAliases.value = false
    }

    fun fetchAliases() {
        if (!_moreAliasesToLoad || _isFetchingAliases) return
        _isFetchingAliases = true
        SLApiService.fetchAliases(apiKey, currentPage + 1) { newAliases, error ->
            _isFetchingAliases = false

            if (error != null) {
                _error.postValue(error)
            } else if (newAliases != null) {
                if (newAliases.isEmpty()) {
                    _moreAliasesToLoad = false
                } else {
                    currentPage += 1
                    aliases.addAll(newAliases)
                    _eventUpdateAliases.postValue(true)
                }
            }
        }
    }

    fun onEventUpdateAliasesComplete() {
        _eventUpdateAliases.value = false
    }
}