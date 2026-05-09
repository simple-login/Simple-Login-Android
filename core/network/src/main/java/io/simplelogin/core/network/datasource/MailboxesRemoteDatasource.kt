package io.simplelogin.core.network.datasource

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.Mailbox
import io.simplelogin.core.model.api.Mailboxes
import io.simplelogin.core.model.api.UpdateMailboxOption
import io.simplelogin.core.network.ApiService
import io.simplelogin.core.network.EmailBody
import io.simplelogin.core.network.TransferAliasesBody
import javax.inject.Inject

interface MailboxesRemoteDatasource {
    suspend fun getMailboxes(apiKey: ApiKey): Result<Mailboxes, ApiError>
    suspend fun createMailbox(apiKey: ApiKey, email: String): Result<Mailbox, ApiError>
    suspend fun deleteMailbox(
        apiKey: ApiKey,
        mailbox: Mailbox,
        transferredMailbox: Mailbox?
    ): Result<Boolean, ApiError>

    suspend fun updateMailbox(
        apiKey: ApiKey,
        mailbox: Mailbox,
        options: UpdateMailboxOption
    ): Result<Boolean, ApiError>
}

class MailboxesRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), MailboxesRemoteDatasource {
    override suspend fun getMailboxes(apiKey: ApiKey): Result<Mailboxes, ApiError> =
        safeApiCall { apiService.getMailboxes(apiKey) }

    override suspend fun createMailbox(apiKey: ApiKey, email: String): Result<Mailbox, ApiError> =
        safeApiCall { apiService.createMailbox(apiKey = apiKey, body = EmailBody(email)) }

    override suspend fun deleteMailbox(
        apiKey: ApiKey,
        mailbox: Mailbox,
        transferredMailbox: Mailbox?
    ): Result<Boolean, ApiError> =
        safeApiCall {
            apiService.deleteMailbox(
                apiKey = apiKey,
                mailboxId = mailbox.id,
                body = TransferAliasesBody(transferredMailbox?.id ?: -1)
            )
        }.mapValue { it.value }

    override suspend fun updateMailbox(
        apiKey: ApiKey,
        mailbox: Mailbox,
        options: UpdateMailboxOption
    ): Result<Boolean, ApiError> =
        safeApiCall {
            apiService.updateMailbox(
                apiKey = apiKey,
                mailboxId = mailbox.id,
                body = options
            )
        }.mapValue { it.value }
}
