package io.simplelogin.android.utils.enums

sealed class SLError(val description: String) {
    object NoData : SLError("Server return no data")
    object IncorrectEmailOrPassword : SLError("Incorrect email or password")
    object InvalidApiKey : SLError("Invalid API key")
    object PageIdRequired: SLError("page_id must be provided in request query")
    object DuplicatedAlias : SLError("Duplicated alias")
    object ReactivationNeeded : SLError("Reactivation needed")
    class BadRequest(description: String) : SLError("Bad request: $description")
    class FailedToParseObject(objectName: String) : SLError("Failed to parse object $objectName")
    object InternalServerError : SLError("Internal server error")
    class UnknownError(description: String) : SLError("Unknown error: $description")
}