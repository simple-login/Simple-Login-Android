package io.simplelogin.android.utils.baseclass

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences

@SuppressLint("Registered")
open class BaseAppCompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SLApiService.setUpBaseUrl(this)
        applyDarkModeIfApplicable()
    }

    private fun applyDarkModeIfApplicable() {
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)

        // Check if force dark mode is enabled:
        if (SLSharedPreferences.getShouldForceDarkMode(this)) {
            // If Dark mode is already enabled, skip
            if (currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        } else {
            val view = window.decorView
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO ->
                    // Night mode is not active, we're using the light theme
                    view.systemUiVisibility = view.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                Configuration.UI_MODE_NIGHT_YES ->
                    // Night mode is active, we're using dark theme
                    view.systemUiVisibility = view.systemUiVisibility and
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
    }
}
