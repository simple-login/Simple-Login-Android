package io.simplelogin.android.domain

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.BlockForward
import io.simplelogin.android.data.models.api.Contact
import io.simplelogin.android.data.models.ui.ContactUiAction
import io.simplelogin.android.data.remote.datasource.AliasDetailsRemoteDatasource
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.ShowSnackbarFailureUseCase
import io.simplelogin.android.usecases.ShowSnackbarInformationUseCase
import io.simplelogin.android.util.description
import javax.inject.Inject

interface ContactUiActionHandler {
    suspend fun handleContactAction(
        apiKey: ApiKey,
        contact: Contact,
        action: ContactUiAction
    ): ContactUiActionResult
}

enum class ContactUiActionResult {
    NONE, BLOCKED, UNBLOCKED, DELETED
}

class ContactUiActionHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val datasource: AliasDetailsRemoteDatasource,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val showSnackbarInformation: ShowSnackbarInformationUseCase,
    private val showSnackbarFailure: ShowSnackbarFailureUseCase,
    private val copyToClipboard: CopyToClipboardUseCase
) : ContactUiActionHandler {
    override suspend fun handleContactAction(
        apiKey: ApiKey,
        contact: Contact,
        action: ContactUiAction
    ): ContactUiActionResult =
        when (action) {
            ContactUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.contacts),
                    content = contact.reverseAlias
                )
                showSnackbarInformation(context.getString(R.string.reverse_alias_copied))
                ContactUiActionResult.NONE
            }

            ContactUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME -> {
                copyToClipboard(
                    label = context.getString(R.string.contacts),
                    content = contact.reverseAliasAddress
                )
                showSnackbarInformation(context.getString(R.string.reverse_alias_copied))
                ContactUiActionResult.NONE
            }

            ContactUiAction.COPY_ADDRESS -> {
                copyToClipboard(
                    label = context.getString(R.string.contacts),
                    content = contact.email
                )
                showSnackbarInformation(context.getString(R.string.email_address_copied))
                ContactUiActionResult.NONE
            }

            ContactUiAction.BLOCK, ContactUiAction.UNBLOCK -> {
                loadingState.value = true
                datasource.toggleContact(apiKey = apiKey, contact = contact)
                    .fold(onSuccess = { blockForward ->
                        handleBlockForward(contact = contact, blockForward = blockForward)
                    }, onFailure = ::handleError)
            }

            ContactUiAction.DELETE -> {
                loadingState.value = true
                datasource.deleteContact(apiKey = apiKey, contact = contact)
                    .fold(onSuccess = { deleted ->
                        loadingState.value = false
                        if (deleted.value) {
                            val message = context.getString(R.string.contact_deleted, contact.email)
                            showSnackbarInformation(message)
                            return@fold ContactUiActionResult.DELETED
                        } else {
                            return@fold ContactUiActionResult.NONE
                        }
                    }, onFailure = ::handleError)
            }

            ContactUiAction.OPEN_DEFAULT_EMAIL_CLIENT -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:${contact.reverseAliasAddress}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.message?.let { showSnackbarFailure(it) }
                }
                ContactUiActionResult.NONE
            }
        }


    private suspend fun handleBlockForward(
        contact: Contact,
        blockForward: BlockForward
    ): ContactUiActionResult {
        loadingState.value = false
        if (blockForward.value) {
            val message = context.getString(R.string.contact_blocked, contact.email)
            showSnackbarInformation(message)
            return ContactUiActionResult.BLOCKED
        } else {
            val message = context.getString(R.string.contact_unblocked, contact.email)
            showSnackbarInformation(message)
            return ContactUiActionResult.UNBLOCKED
        }
    }

    private suspend fun handleError(error: ApiError): ContactUiActionResult {
        loadingState.value = false
        showSnackbarFailure(error.description(context))
        return ContactUiActionResult.NONE
    }
}
