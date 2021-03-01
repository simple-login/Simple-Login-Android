package io.simplelogin.android.utils

import android.content.Context
import android.content.SharedPreferences

object SLSharedPreferences {
    private const val PREFERENCE_FILE_KEY = "io.simplelogin.android"

    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    fun reset(context: Context) {
        removeApiKey(context)
        setShouldForceDarkMode(context, false)
        setShouldLocallyAuthenticate(context, false)
    }

    //region API KEY
    private const val API_KEY = "API_KEY"

    fun getApiKey(context: Context) : String? =
        getSharedPreferences(context).getString(API_KEY, null)

    fun setApiKey(context: Context, apiKey: String) {
        with(getSharedPreferences(context).edit()) {
            putString(API_KEY, apiKey)
            commit()
        }
    }

    private fun removeApiKey(context: Context) {
        with(getSharedPreferences(context).edit()) {
            remove(API_KEY)
            commit()
        }
    }
    //endregion

    //region API URL
    private const val API_URL = "API_URL"
    private const val defaultApiUrl = "https://app.simplelogin.io"

    fun getApiUrl(context: Context) : String =
        getSharedPreferences(context).getString(API_URL, null) ?: defaultApiUrl

    fun setApiUrl(context: Context, apiUrl: String) {
        with(getSharedPreferences(context).edit()) {
            putString(API_URL, apiUrl)
            commit()
        }
    }

    fun resetApiUrl(context: Context) {
        with(getSharedPreferences(context).edit()) {
            putString(API_URL, defaultApiUrl)
            commit()
        }
    }
    //endregion

    //region RATED
    private const val RATED = "RATED"

    fun getRated(context: Context) : Boolean =
        getSharedPreferences(context).getBoolean(RATED, false)

    fun setRated(context: Context, rated: Boolean = true) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(RATED, rated)
            commit()
        }
    }
    //endregion

    //region DARK MODE
    private const val FORCE_DARK_MODE = "FORCE_DARK_MODE"

    fun getShouldForceDarkMode(context: Context) : Boolean =
        getSharedPreferences(context).getBoolean(FORCE_DARK_MODE, false)

    fun setShouldForceDarkMode(context: Context, shouldForceDarkMode: Boolean) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(FORCE_DARK_MODE, shouldForceDarkMode)
            commit()
        }
    }
    //endregion

    //region LOCAL AUTHENTICATION
    private const val SHOULD_LOCALLY_AUTHENTICATE = "SHOULD_LOCALLY_AUTHENTICATE"

    fun getShouldLocallyAuthenticate(context: Context) : Boolean =
        getSharedPreferences(context).getBoolean(SHOULD_LOCALLY_AUTHENTICATE, false)

    fun setShouldLocallyAuthenticate(context: Context, shouldLocallyAuthenticate: Boolean) {
        with(getSharedPreferences(context).edit()) {
            putBoolean(SHOULD_LOCALLY_AUTHENTICATE, shouldLocallyAuthenticate)
            commit()
        }
    }
    //endregion
}
