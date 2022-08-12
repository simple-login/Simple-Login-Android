package io.simplelogin.android.utils

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import io.simplelogin.android.R
import io.simplelogin.android.utils.model.TemporaryToken


object LoginWithProtonUtils {
    fun launchLoginWithProton(context: Context) {
        val baseUrl = SLSharedPreferences.getApiUrl(context)
        val scheme = context.getString(R.string.simplelogin_scheme)
        val next = "/login"
        val url = "${baseUrl}/auth/proton/login?mode=apikey&action=login&scheme=${scheme}&next=${next}"
        launchChromeTab(context, url)
    }

    fun launchLinkWithProton(context: Context, temporaryToken: TemporaryToken) {
        val baseUrl = SLSharedPreferences.getApiUrl(context)
        val scheme = context.getString(R.string.simplelogin_scheme)
        val action = "link"
        val next = "/link"
        val nextQuery = "/auth/proton/login?action=${action}&next=${next}&scheme=${scheme}"
        val nextQueryEncoded = Uri.encode(nextQuery)
        val url = "${baseUrl}/auth/api_to_cookie?token=${temporaryToken.token}&next=${nextQueryEncoded}"
        launchChromeTab(context, url)
    }

    private fun launchChromeTab(context: Context, url: String) {
        val builder = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
                .setToolbarColor(R.color.protonMain)
                .build())

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}