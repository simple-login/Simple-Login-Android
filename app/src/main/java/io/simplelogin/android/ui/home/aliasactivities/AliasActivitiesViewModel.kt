package io.simplelogin.android.ui.home.aliasactivities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.PAGE_SIZE
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.ui.ActivityUiAction
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.domain.ActivityUiActionHandler
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

@HiltViewModel(assistedFactory = AliasActivitiesViewModel.Factory::class)
class AliasActivitiesViewModel @AssistedInject constructor(
    @Assisted private val alias: Alias,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val datasource: AliasesRemoteDatasource,
    private val actionHandler: ActivityUiActionHandler
) : ViewModel() {
    private var apiKey: ApiKey? = null

    private val _stateFlow = MutableStateFlow(AliasActivitiesState.Default)
    val stateFlow: StateFlow<AliasActivitiesState> = _stateFlow

    @AssistedFactory
    interface Factory {
        fun create(alias: Alias): AliasActivitiesViewModel
    }

    fun refresh() {
        _stateFlow.update {
            it.copy(isRefreshing = true, isLoadingMore = false, error = null)
        }
        getActivities(page = 0, process = { it })
    }

    fun retry() {
        _stateFlow.update {
            it.copy(isRefreshing = false, isLoadingMore = true, error = null)
        }
        val currentPage = _stateFlow.value.page
        val currentActivities = _stateFlow.value.activities
        getActivities(page = currentPage, process = { currentActivities + it })
    }

    fun loadMoreIfNeed() {
        if (!_stateFlow.value.canLoadMore) return
        _stateFlow.update {
            it.copy(isRefreshing = false, isLoadingMore = true, error = null)
        }
        val page = _stateFlow.value.page + 1
        val currentActivities = _stateFlow.value.activities
        getActivities(page = page, process = { currentActivities + it })
    }

    private fun getActivities(page: Int, process: (List<AliasActivity>) -> List<AliasActivity>) {
        withApiKey { apiKey ->
            datasource.getActivities(apiKey = apiKey, aliasId = alias.id, page = page)
                .fold(onSuccess = { activities ->
                    val processedActivities = process(activities)
                    _stateFlow.update {
                        it.copy(
                            activities = processedActivities,
                            page = page,
                            canLoadMore = activities.count() >= PAGE_SIZE,
                            isRefreshing = false,
                            isLoadingMore = false
                        )
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(
                            isRefreshing = false,
                            isLoadingMore = false,
                            canLoadMore = false,
                            error = error
                        )
                    }
                })
        }
    }

    fun handleAction(activity: AliasActivity, action: ActivityUiAction) {
        viewModelScope.launch {
            actionHandler.handleActivityAction(activity = activity, action = action)
        }
    }

    private fun withApiKey(perform: suspend (ApiKey) -> Unit) {
        viewModelScope.launch {
            apiKey?.let { perform(it) } ?: observeSessionSettings()
                .mapNotNull { it.apiKey }
                .collect {
                    apiKey = it
                    perform(it)
                }
        }
    }
}