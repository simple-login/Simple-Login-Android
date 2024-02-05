package io.simplelogin.android.module.alias.contact

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.simplelogin.android.utils.model.Alias
import java.lang.IllegalArgumentException

class ContactListViewModelFactory (private val context: Context, private val alias: Alias) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactListViewModel::class.java)) {
            return ContactListViewModel(context, alias) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
