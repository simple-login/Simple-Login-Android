package io.simplelogin.android.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.models.api.ActivityAction
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.ui.ActivityUiAction
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.ShowSnackbarFailureUseCase
import io.simplelogin.android.usecases.ShowSnackbarInformationUseCase
import javax.inject.Inject

interface ActivityUiActionHandler {
    suspend fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction)
}

class ActivityUiActionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val showSnackbarInformation: ShowSnackbarInformationUseCase,
    private val showSnackbarFailure: ShowSnackbarFailureUseCase,
    private val copyToClipboard: CopyToClipboardUseCase
) : ActivityUiActionHandler {
    override suspend fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction) {
        when (action) {
            ActivityUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.alias_activity),
                    content = activity.reverseAlias
                )
                showSnackbarInformation(context.getString(R.string.reverse_alias_copied))
            }

            ActivityUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.alias_activity),
                    content = activity.reverseAliasAddress
                )
                showSnackbarInformation(context.getString(R.string.reverse_alias_copied))
            }

            ActivityUiAction.COPY_ADDRESS -> {
                val address = when (activity.action) {
                    ActivityAction.REPLY -> activity.to
                    else -> activity.from
                }
                copyToClipboard(
                    label = context.getString(R.string.alias_activity),
                    content = address
                )
                showSnackbarInformation(context.getString(R.string.email_address_copied))
            }

            ActivityUiAction.OPEN_DEFAULT_EMAIL_CLIENT -> {
                val address = when (activity.action) {
                    ActivityAction.REPLY -> activity.to
                    else -> activity.from
                }

                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${address}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.message?.let { showSnackbarFailure(it) }
                }
            }
        }
    }
}