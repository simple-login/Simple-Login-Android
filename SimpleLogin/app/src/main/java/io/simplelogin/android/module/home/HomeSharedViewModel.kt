package io.simplelogin.android.module.home

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.model.Alias
import java.lang.IllegalStateException

class HomeSharedViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKey: String by lazy {
        SLSharedPreferences.getApiKey(application) ?: throw IllegalStateException("API key is null")
    }

    // Aliases
    private var currentPage = -1
    private var _moreAliasesToLoad: Boolean = true
    val moreAliasesToLoad: Boolean
        get() = _moreAliasesToLoad

    private val _aliases = MutableLiveData<MutableList<Alias>>()
    val aliases: LiveData<MutableList<Alias>>
        get() = _aliases

    private val _isFetchingAliases = MutableLiveData<Boolean>()
    val isFetchingAliases: LiveData<Boolean>
        get() = _isFetchingAliases

    init {
        _aliases.value = mutableListOf()
        _isFetchingAliases.value = false
    }

    fun fetchAliases() {
        if (!_moreAliasesToLoad) return

        SLApiService.fetchAliases(apiKey, currentPage + 1) { aliases, error ->
            if (error != null) {
                Toast.makeText(getApplication(), error.description, Toast.LENGTH_SHORT).show()
            } else if (aliases != null) {
                if (aliases.isEmpty()) {
                    _moreAliasesToLoad = false
                } else {
                    currentPage += 1
                    _aliases.value?.addAll(aliases)
                }
            }
        }
    }
}