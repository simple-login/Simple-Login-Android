package io.simplelogin.android.module.alias

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.enums.AliasFilterMode
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.Contact
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
        SLApiService.fetchAliases(apiKey, currentPage + 1) { result ->
            _isFetchingAliases = false

            result.onSuccess { newAliases ->
                if (newAliases.isEmpty()) {
                    moreAliasesToLoad = false
                    _eventUpdateAliases.postValue(true)
                } else {
                    currentPage += 1
                    _aliases.addAll(newAliases.filter { !_deletedAliasIds.contains(it.id) })
                    filterAliases()
                }
            }

            result.onFailure { _error.postValue(it as SLError) }
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
    fun deleteAlias(alias: Alias) {
        SLApiService.deleteAlias(apiKey, alias) { result ->
            result.onSuccess {
                _deletedAliasIds.add(alias.id)
                _aliases.removeAll { it.id == alias.id }
                filterAliases()
            }

            result.onFailure { _error.postValue(it as SLError) }
        }
    }

    // Toggle
    private var _toggledAliasIndex = MutableLiveData<Int>()
    val toggledAliasIndex: LiveData<Int>
        get() = _toggledAliasIndex

    fun onHandleToggleAliasComplete() {
        _toggledAliasIndex.value = null
    }

    fun toggleAlias(alias: Alias, index: Int) {
        SLApiService.toggleAlias(apiKey, alias) { result ->
            result.onSuccess { enabled ->
                _aliases.find { it.id == alias.id }?.setEnabled(enabled.value)
                _toggledAliasIndex.postValue(index)
                filterAliases()
            }

            result.onFailure { _error.postValue(it as SLError) }
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

    // Handle mailto email
    private var _shouldActionOnMailToEmail = MutableLiveData<Boolean>()
    val shouldActionOnMailToEmail: LiveData<Boolean>
        get() = _shouldActionOnMailToEmail
    var mailToEmail: String? = null
        private set

    fun getMailToEmail(intent: Intent) {
        val email = intent.getStringExtra(HomeActivity.EMAIL)
        if (email != null && email.isNotEmpty()) {
            mailToEmail = email
            _shouldActionOnMailToEmail.value = true
            intent.removeExtra(HomeActivity.EMAIL)
        }
    }

    fun onActionOnMailToEmailComplete() {
        _shouldActionOnMailToEmail.value = false
    }

    private var _mailFromAlias = MutableLiveData<Alias>()
    val mailFromAlias: LiveData<Alias>
        get() = _mailFromAlias

    fun setMailFromAlias(alias: Alias) {
        _mailFromAlias.value = alias
    }

    private var _createdContact = MutableLiveData<Contact>()
    val createdContact: LiveData<Contact>
        get() = _createdContact

    fun createContact(alias: Alias) {
        val mailToEmail = mailToEmail ?: return
        SLApiService.createContact(apiKey, alias, mailToEmail) { result ->
            result.onSuccess {
                _createdContact.postValue(it)
                _mailFromAlias.postValue(null)
            }
            result.onFailure {
                _error.postValue(it as SLError)
                _mailFromAlias.postValue(null)
            }
        }
    }

    fun onHandleCreatedContactComplete() {
        _createdContact.value = null
        _mailFromAlias.value = null
    }
}
