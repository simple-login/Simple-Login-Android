package io.simplelogin.android.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.ui.ActivityUiAction
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import javax.inject.Inject

interface ActivityUiActionHandler {
    suspend fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction)
}

class ActivityUiActionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snackbarManager: SnackbarManager,
    private val copyToClipboard: CopyToClipboardUseCase
) : ActivityUiActionHandler {
    override suspend fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction) {
        when (action) {
            ActivityUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.alias_activity),
                    content = activity.reverseAlias
                )
                val message = context.getString(R.string.reverse_alias_copied)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
            }

            ActivityUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.alias_activity),
                    content = activity.reverseAliasAddress
                )
                val message = context.getString(R.string.reverse_alias_copied)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
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
                val message = context.getString(R.string.email_address_copied)
                snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
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
                    e.message?.let {
                        snackbarManager.showSnackbar(
                            SnackbarConfiguration(
                                message = it,
                                type = SnackbarType.FAILURE
                            )
                        )
                    }
                }
            }
        }
    }
}