package io.simplelogin.android.utils.baseclass

import android.content.Context
import androidx.lifecycle.ViewModel
import io.simplelogin.android.utils.SLSharedPreferences
import java.lang.IllegalStateException

open class BaseViewModel(context: Context) : ViewModel() {
    val apiKey: String by lazy {
        SLSharedPreferences.getApiKey(context) ?: throw IllegalStateException("API key is null")
    }
}
