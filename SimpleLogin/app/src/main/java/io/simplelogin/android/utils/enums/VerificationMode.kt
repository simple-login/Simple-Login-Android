package io.simplelogin.android.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class VerificationMode {
    @Parcelize
    class Mfa(val mfaKey: MfaKey) : VerificationMode(), Parcelable

    @Parcelize
    class AccountActivation(val email: Email, val password: Password) : VerificationMode(), Parcelable
}

@Suppress("ForbiddenComment")
// TODO: Should be inline class
@Parcelize
class MfaKey(val value: String): Parcelable

@Parcelize
class Email(val value: String): Parcelable

@Parcelize
class Password(val value: String): Parcelable
