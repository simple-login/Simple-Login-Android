package io.simplelogin.android.data.models.ui

sealed class AliasAction {
    data class ViewDetails(val id: Int): AliasAction()
    data class ViewContacts(val id: Int): AliasAction()
    data class CopyEmailAddress(val email: String): AliasAction()
    data class Enable(val id: Int): AliasAction()
    data class Disable(val id: Int): AliasAction()
    data class Delete(val id: Int): AliasAction()
}