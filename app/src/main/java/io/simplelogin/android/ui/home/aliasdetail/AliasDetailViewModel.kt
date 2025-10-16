package io.simplelogin.android.ui.home.aliasdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AliasDetailViewModel.Factory::class)
class AliasDetailViewModel @AssistedInject constructor(
    @Assisted val alias: Alias,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val remoteDatasource: AliasesRemoteDatasource,
    observeDeviceSettingsUseCase: ObserveDeviceSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(alias: Alias): AliasDetailViewModel
    }

    private val activitiesStateFlow =
        MutableStateFlow<AliasActivitiesState>(AliasActivitiesState.Loading)

    val stateFlow = combine(
        observeDeviceSettingsUseCase(),
        activitiesStateFlow
    ) { deviceSettings, activities ->
        AliasDetailScreenState(
            devicePreferences = deviceSettings,
            activitiesState = activities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AliasDetailScreenState.Default
    )

    init {
        viewModelScope.launch {
            getActivities()
        }
    }

    suspend fun getActivities() {
        activitiesStateFlow.value = AliasActivitiesState.Loading
        observeSessionSettings()
            .collect { settings ->
                val apiKey = settings.apiKey
                assert(apiKey != null) { "API key is null" }
                apiKey?.let {
                    remoteDatasource.getActivities(
                        apiKey = apiKey,
                        aliasId = alias.id,
                        page = 0
                    ).fold(
                        onSuccess = {
                            activitiesStateFlow.value = AliasActivitiesState.Loaded(it)
                        },
                        onFailure = {
                            activitiesStateFlow.value = AliasActivitiesState.Error(it)
                        }
                    )
                }
            }
    }
}