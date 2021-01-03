package io.simplelogin.android.module.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.DispatchGroup
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.DomainLite
import io.simplelogin.android.utils.model.UserSettings

class SettingsViewModel(val context: Context) : BaseViewModel(context) {
    // Error
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    // UserSettings
    private lateinit var _userSettings: UserSettings
    val userSettings: UserSettings
        get() = _userSettings

    private val _evenUserSettingsUpdated = MutableLiveData<Boolean>()
    val evenUserSettingsUpdated: LiveData<Boolean>
        get() = _evenUserSettingsUpdated

    fun onHandleUserSettingsUpdatedComplete() {
        _evenUserSettingsUpdated.value = false
    }

    // DomainLite list
    private var _domainLites = listOf<DomainLite>()
    val domainLites: List<DomainLite>
        get() = _domainLites

    private var _isFetching = MutableLiveData<Boolean>()
    val isFetching: LiveData<Boolean>
        get() = _isFetching

    fun fetchUserSettingsAndDomainLites() {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)

        val dispatchGroup = DispatchGroup()
        var storedError: SLError? = null

        // Fetch UserSettings
        dispatchGroup.enter()
        SLApiService.fetchUserSettings(apiKey) { result ->
            dispatchGroup.leave()
            result.onSuccess { _userSettings = it }
            result.onFailure { storedError = it as SLError }
        }

        // Fetch DomainLites
        dispatchGroup.enter()
        SLApiService.fetchDomainLites(apiKey) { result ->
            dispatchGroup.leave()
            result.onSuccess { _domainLites = it }
            result.onFailure { storedError = it as SLError }
        }

        dispatchGroup.notify {
            _isFetching.postValue(false)
            if (storedError != null) {
                _error.postValue(storedError)
                return@notify
            }
            _evenUserSettingsUpdated.postValue(true)
        }
    }

    // Update
    fun updateUserSettings(option: UserSettings.Option) {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)

        SLApiService.updateUserSettings(apiKey, option) { result ->
            _isFetching.postValue(false)
            result.onSuccess { _userSettings = it }
            result.onFailure { _error.postValue(it as SLError) }
            _evenUserSettingsUpdated.postValue(true)
        }
    }
}