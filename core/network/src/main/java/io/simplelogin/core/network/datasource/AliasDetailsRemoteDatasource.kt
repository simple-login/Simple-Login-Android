package io.simplelogin.core.network.datasource

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasActivity
import io.simplelogin.core.model.api.AliasId
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.BlockForward
import io.simplelogin.core.model.api.Contact
import io.simplelogin.core.model.api.Mailbox
import io.simplelogin.core.model.api.UpdateAliasOption
import io.simplelogin.core.network.ApiService
import io.simplelogin.core.network.CreateContactBody
import io.simplelogin.core.network.DeletedResponse
import javax.inject.Inject

interface AliasDetailsRemoteDatasource {
    suspend fun getAlias(apiKey: ApiKey, aliasId: AliasId): Result<Alias, ApiError>
    suspend fun getActivities(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<AliasActivity>, ApiError>

    suspend fun getContacts(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<Contact>, ApiError>

    suspend fun update(
        apiKey: ApiKey,
        aliasId: AliasId,
        option: UpdateAliasOption
    ): Result<Unit, ApiError>

    suspend fun toggleContact(apiKey: ApiKey, contact: Contact): Result<BlockForward, ApiError>

    suspend fun deleteContact(apiKey: ApiKey, contact: Contact): Result<DeletedResponse, ApiError>

    suspend fun createContact(
        apiKey: ApiKey,
        aliasId: AliasId,
        email: String
    ): Result<Contact, ApiError>
}

suspend fun AliasDetailsRemoteDatasource.updateNote(
    apiKey: ApiKey,
    aliasId: AliasId,
    note: String
) =
    update(apiKey = apiKey, aliasId = aliasId, option = UpdateAliasOption.Note(note))

suspend fun AliasDetailsRemoteDatasource.updateName(
    apiKey: ApiKey,
    aliasId: AliasId,
    name: String
) =
    update(apiKey = apiKey, aliasId = aliasId, option = UpdateAliasOption.Name(name))

suspend fun AliasDetailsRemoteDatasource.pin(
    apiKey: ApiKey,
    aliasId: AliasId
): Result<Unit, ApiError> =
    update(apiKey = apiKey, aliasId = aliasId, option = UpdateAliasOption.Pinned(true))

suspend fun AliasDetailsRemoteDatasource.unpin(
    apiKey: ApiKey,
    aliasId: AliasId
): Result<Unit, ApiError> =
    update(apiKey = apiKey, aliasId = aliasId, option = UpdateAliasOption.Pinned(false))

suspend fun AliasDetailsRemoteDatasource.updateMailboxes(
    apiKey: ApiKey,
    aliasId: AliasId,
    mailboxes: List<Mailbox>
) =
    update(
        apiKey = apiKey,
        aliasId = aliasId,
        option = UpdateAliasOption.Mailboxes(mailboxes.map { it.id })
    )

class AliasDetailsRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), AliasDetailsRemoteDatasource {
    override suspend fun getAlias(apiKey: ApiKey, aliasId: AliasId): Result<Alias, ApiError> =
        safeApiCall { apiService.getAlias(apiKey = apiKey, aliasId = aliasId) }

    override suspend fun getActivities(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<AliasActivity>, ApiError> =
        safeApiCall {
            apiService.getAliasActivities(apiKey = apiKey, aliasId = aliasId, pageId = page)
        }.mapValue { it.activities }

    override suspend fun getContacts(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<Contact>, ApiError> =
        safeApiCall {
            apiService.getContacts(apiKey = apiKey, aliasId = aliasId, pageId = page)
        }.mapValue { it.contacts }

    override suspend fun update(
        apiKey: ApiKey,
        aliasId: AliasId,
        option: UpdateAliasOption
    ): Result<Unit, ApiError> =
        safeApiCall {
            apiService.updateAlias(apiKey = apiKey, aliasId = aliasId, body = option)
        }.mapValue { }

    override suspend fun toggleContact(
        apiKey: ApiKey,
        contact: Contact
    ): Result<BlockForward, ApiError> =
        safeApiCall { apiService.toggleContact(apiKey = apiKey, contactId = contact.id) }

    override suspend fun deleteContact(
        apiKey: ApiKey,
        contact: Contact
    ): Result<DeletedResponse, ApiError> =
        safeApiCall { apiService.deleteContact(apiKey = apiKey, contactId = contact.id) }

    override suspend fun createContact(
        apiKey: ApiKey,
        aliasId: AliasId,
        email: String
    ): Result<Contact, ApiError> =
        safeApiCall {
            apiService.createContact(
                apiKey = apiKey,
                aliasId = aliasId,
                body = CreateContactBody(contact = email)
            )
        }
}