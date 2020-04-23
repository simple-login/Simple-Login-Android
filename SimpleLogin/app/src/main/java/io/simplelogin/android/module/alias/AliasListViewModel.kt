package io.simplelogin.android.module.alias

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import java.lang.IllegalStateException

class AliasListViewModel(application: Application) : AndroidViewModel(application) {
    val apiKey: String by lazy {
        SLSharedPreferences.getApiKey(application) ?: throw IllegalStateException("API key is null")
    }
    private val _error = MutableLiveData<SLError>()
    val error: LiveData<SLError>
        get() = _error

    fun onHandleErrorComplete() {
        _error.value = null
    }

    private var currentPage = -1
    var moreAliasesToLoad: Boolean = true
        private set

    private var _aliases = mutableListOf<Alias>()
    private var _deletedAliasIds = mutableListOf<Int>()
    var filteredAliases = listOf<Alias>()
        private set

    private var _isFetchingAliases = false

    private val _eventUpdateAliases = MutableLiveData<Boolean>()
    val eventUpdateAliases: LiveData<Boolean>
        get() = _eventUpdateAliases

    fun onEventUpdateAliasesComplete() {
        _eventUpdateAliases.value = false
    }

    fun fetchAliases() {
        if (!moreAliasesToLoad || _isFetchingAliases) return
        _isFetchingAliases = true
        SLApiService.fetchAliases(apiKey, currentPage + 1) { newAliases, error ->
            _isFetchingAliases = false

            if (error != null) {
                _error.postValue(error)
            } else if (newAliases != null) {
                if (newAliases.isEmpty()) {
                    moreAliasesToLoad = false
                    _eventUpdateAliases.postValue(true)
                } else {
                    currentPage += 1
                    _aliases.addAll(newAliases.filter { !_deletedAliasIds.contains(it.id) })
                    filterAliases()
                }
            }
        }
    }

    fun refreshAliases() {
        currentPage = -1
        moreAliasesToLoad = true
        _aliases = mutableListOf()
        filteredAliases = listOf()
        _deletedAliasIds = mutableListOf()
        fetchAliases()
    }

    // Filter
    var aliasFilterMode = AliasFilterMode.ALL
        private set

    fun filterAliases(mode: AliasFilterMode? = null) {
        mode?.let {
            aliasFilterMode = it
        }

        filteredAliases = when (aliasFilterMode) {
            AliasFilterMode.ALL -> _aliases
            AliasFilterMode.ACTIVE -> _aliases.filter { it.enabled }
            AliasFilterMode.INACTIVE -> _aliases.filter { !it.enabled }
        }

        _eventUpdateAliases.postValue(true)
    }

    // Delete
    fun deleteAlias(alias: Alias, firebaseAnalytics: FirebaseAnalytics) {
        SLApiService.deleteAlias(apiKey, alias) { error ->
            if (error != null) {
                _error.postValue(error)
                firebaseAnalytics.logEvent("alias_list_delete_error", error.toBundle())
            } else {
                _deletedAliasIds.add(alias.id)
                _aliases.removeAll { it.id == alias.id }
                filterAliases()
                firebaseAnalytics.logEvent("alias_list_delete_success", null)
            }
        }
    }

    // Toggle
    private var _toggledAliasIndex = MutableLiveData<Int>()
    val toggledAliasIndex: LiveData<Int>
        get() = _toggledAliasIndex

    fun onHandleToggleAliasComplete() {
        _toggledAliasIndex.value = null
    }

    fun toggleAlias(alias: Alias, index: Int, firebaseAnalytics: FirebaseAnalytics) {
        SLApiService.toggleAlias(apiKey, alias) { enabled, error ->
            if (error != null) {
                _error.postValue(error)
                firebaseAnalytics.logEvent("alias_list_toggle_error", error.toBundle())
            } else if (enabled != null) {
                _aliases.find { it.id == alias.id }?.setEnabled(enabled)
                _toggledAliasIndex.postValue(index)
                filterAliases()

                when (enabled) {
                    true -> firebaseAnalytics.logEvent("alias_list_enabled_an_alias", null)
                    false -> firebaseAnalytics.logEvent("alias_list_disabled_an_alias", null)
                }
            }
        }
    }

    // Update toggled and deleted aliases
    fun updateToggledAndDeletedAliases(toggledAliases: List<Alias>, deletedIds: List<Int>) {
        _deletedAliasIds.addAll(deletedIds)
        deletedIds.forEach { deletedId ->
            _aliases.removeAll { it.id == deletedId }
        }

        toggledAliases.forEach { alias ->
            _aliases.find { it.id == alias.id }?.setEnabled(alias.enabled)
        }
    }

    // Add
    fun addAlias(alias: Alias) {
        _aliases.add(0, alias)
    }

    // Update
    fun updateAlias(alias: Alias) {
        val index = _aliases.indexOfFirst { it.id == alias.id }
        _aliases[index] = alias
    }

    // Show pricing
    var needsShowPricing = false
        private set

    fun onHandleShowPricingComplete() {
        needsShowPricing = false
    }

    fun setNeedsSeePricing() {
        needsShowPricing = true
    }

    // Save scrolling position
    private var _lastScrollingPosition: Int = 0
    fun getLastScrollingPosition() = _lastScrollingPosition
    fun setLastScrollingPosition(position: Int) {
        _lastScrollingPosition = position
    }
}