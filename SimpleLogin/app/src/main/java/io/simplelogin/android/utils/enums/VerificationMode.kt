package io.simplelogin.android.utils.enums

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class VerificationMode {
    @Parcelize
    class Mfa(val mfaKey: String) : VerificationMode(), Parcelable

    @Parcelize
    class AccountActivation(val email: String, val password: String) : VerificationMode(), Parcelable
}