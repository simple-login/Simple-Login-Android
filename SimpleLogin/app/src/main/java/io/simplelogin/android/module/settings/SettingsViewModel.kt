package io.simplelogin.android.module.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.DispatchGroup
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseViewModel
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.DomainLite
import io.simplelogin.android.utils.model.TemporaryToken
import io.simplelogin.android.utils.model.UserInfo
import io.simplelogin.android.utils.model.UserSettings

class SettingsViewModel(val context: Context) : BaseViewModel(context) {
    // Error
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    // UserInfo
    lateinit var userInfo: UserInfo
        private set

    private val _eventUserInfoUpdated = MutableLiveData<Boolean>()
    val eventUserInfoUpdated: LiveData<Boolean>
        get() = _eventUserInfoUpdated

    fun setUserInfo(userInfo: UserInfo) {
        this.userInfo = userInfo
        _eventUserInfoUpdated.postValue(true)
    }

    fun updateName(name: String?) {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)
        SLApiService.updateName(apiKey, name) { handleUserInfoResult(it) }
    }

    fun removeProfilePhoto() {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)
        SLApiService.updateProfilePhoto(apiKey, null) { handleUserInfoResult(it) }
    }

    fun updateProfilePhoto(base64String: String) {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)
        SLApiService.updateProfilePhoto(apiKey, base64String) { handleUserInfoResult(it) }
    }

    private fun handleUserInfoResult(result: Result<UserInfo>) {
        _isFetching.postValue(false)
        result.onSuccess { setUserInfo(it) }
        result.onFailure { _error.postValue(it as SLError) }
    }

    fun onHandleUserInfoUpdatedComplete() {
        _eventUserInfoUpdated.value = false
    }

    // UserSettings
    lateinit var userSettings: UserSettings
        private set

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
            result.onSuccess { userSettings = it }
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

    fun refreshUserInfo(
        ignoreIsFetching: Boolean = false,
        onCompletion: () -> Unit = {}
    ) {
        if (!ignoreIsFetching) {
            if (_isFetching.value == true) return
            _isFetching.postValue(true)
        }

        SLApiService.fetchUserInfo(apiKey) { result ->
            _isFetching.postValue(false)
            result.onSuccess {
                userInfo = it
                onCompletion()
            }
            result.onFailure { _error.postValue(it as SLError) }
            _eventUserInfoUpdated.postValue(true)
        }
    }

    // Update
    fun updateUserSettings(option: UserSettings.Option) {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)

        SLApiService.updateUserSettings(apiKey, option) { result ->
            _isFetching.postValue(false)
            result.onSuccess { userSettings = it }
            result.onFailure { _error.postValue(it as SLError) }
            _evenUserSettingsUpdated.postValue(true)
        }
    }

    // Unlink Proton account
    private val _eventProtonAccountUnlinked = MutableLiveData<Boolean>()
    val eventProtonAccountUnlinked: LiveData<Boolean>
        get() = _eventProtonAccountUnlinked

    fun unlinkProtonAccount() {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)

        SLApiService.unlinkProtonAccount(apiKey) { result ->
            // Refresh the user info but do not take into account the isFetching value,
            // as we don't want the loading dialog to flash
            result.onSuccess {
                refreshUserInfo(ignoreIsFetching = true) {
                    _eventProtonAccountUnlinked.postValue(true)
                }
            }
            result.onFailure {
                _error.postValue(it as SLError)
                _isFetching.postValue(false)
            }
        }
    }

    fun getTemporaryToken(completion: (TemporaryToken) -> Unit) {
        if (_isFetching.value == true) return
        _isFetching.postValue(true)
        SLApiService.getTemporaryToken(apiKey) { result ->
            result.onSuccess { completion(it) }
            result.onFailure { _error.postValue(it as SLError) }
            _isFetching.postValue(false)
        }
    }
}