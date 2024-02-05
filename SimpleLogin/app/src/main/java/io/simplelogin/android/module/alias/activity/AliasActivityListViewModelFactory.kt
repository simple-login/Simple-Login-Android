package io.simplelogin.android.module.alias.activity

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.simplelogin.android.utils.model.Alias

class AliasActivityListViewModelFactory(private val context: Context, private val alias: Alias) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AliasActivityListViewModel::class.java)) {
            return AliasActivityListViewModel(context, alias) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
