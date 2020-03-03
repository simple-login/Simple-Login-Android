package io.simplelogin.android.utils

import android.content.Context
import android.content.SharedPreferences

class SLSharedPreferences {
    companion object {
        private const val PREFERENCE_FILE_KEY = "io.simplelogin.android"
        private const val API_KEY = "API_KEY"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        }

        fun getApiKey(context: Context) : String? {
            return getSharedPreferences(context).getString(API_KEY, null)
        }

        fun setApiKey(context: Context, apiKey: String) {
            with(getSharedPreferences(context).edit()) {
                putString(API_KEY, apiKey)
                commit()
            }
        }

        fun removeApiKey(context: Context) {
            with(getSharedPreferences(context).edit()) {
                remove(API_KEY)
                commit()
            }
        }
    }
}