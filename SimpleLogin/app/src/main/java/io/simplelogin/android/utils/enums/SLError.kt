package io.simplelogin.android.utils.enums

sealed class SLError(val description: String) : Throwable(description) {
    object NoData : SLError("Server return no data")
    object IncorrectEmailOrPassword : SLError("Incorrect email or password")
    object InvalidApiKey : SLError("Invalid API key")
    object PageIdRequired : SLError("page_id must be provided in request query")
    object DuplicatedAlias : SLError("Duplicated alias")
    object DuplicatedContact : SLError("Duplicated contact")
    object ReactivationNeeded : SLError("Reactivation needed")
    object InternalServerError : SLError("Internal server error")
    object BadGateway : SLError("Bad gateway error")
    object SearchTermNull : SLError("Search term is null")
    object WrongTotpToken : SLError("Wrong TOTP token")
    object CanNotCreateMoreAlias : SLError("Can not create more alias")
    object WrongVerificationCode : SLError("Wrong verification code")
    class BadRequest(description: String) : SLError("Bad request: $description")
    class FailedToParse(any: Any) : SLError("Failed to parse ${any.javaClass.kotlin}")
    class ResponseError(code: Int) : SLError("Response error code $code")
    class UnknownError(description: String) : SLError("Unknown error: $description")
}