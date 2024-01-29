package io.simplelogin.android.module.alias.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class AliasSearchViewModelFactory (private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AliasSearchViewModel::class.java)) {
            return AliasSearchViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
