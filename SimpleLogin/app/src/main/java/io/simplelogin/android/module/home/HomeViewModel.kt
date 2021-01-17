package io.simplelogin.android.module.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.model.UserInfo

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _eventUserInfoUpdated = MutableLiveData<Boolean>()
    val eventUserInfoUpdated: LiveData<Boolean>
        get() = _eventUserInfoUpdated

    lateinit var userInfo: UserInfo
        private set

    fun setUserInfo(userInfo: UserInfo) {
        this.userInfo = userInfo
        _eventUserInfoUpdated.value = true
    }

    fun onHandleUserInfoUpdateComplete() {
        _eventUserInfoUpdated.value = false
    }
}