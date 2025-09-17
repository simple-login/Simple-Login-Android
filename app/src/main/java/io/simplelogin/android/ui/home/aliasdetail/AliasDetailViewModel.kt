package io.simplelogin.android.ui.home.aliasdetail

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.Alias

@HiltViewModel(assistedFactory = AliasDetailViewModel.Factory::class)
class AliasDetailViewModel @AssistedInject constructor(
    @Assisted val alias: Alias
): ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(alias: Alias): AliasDetailViewModel
    }
}