package io.simplelogin.core.model.ui

import io.simplelogin.core.model.api.Alias

sealed class AliasAction(open val alias: Alias) {
    data class ViewDetails(override val alias: Alias) : AliasAction(alias)
    data class ViewContacts(override val alias: Alias) : AliasAction(alias)
    data class CopyEmailAddress(override val alias: Alias) : AliasAction(alias)
    data class EnterFullScreen(override val alias: Alias) : AliasAction(alias)
    data class Enable(override val alias: Alias) : AliasAction(alias)
    data class Disable(override val alias: Alias) : AliasAction(alias)
    data class Pin(override val alias: Alias) : AliasAction(alias)
    data class Unpin(override val alias: Alias) : AliasAction(alias)
    data class Delete(override val alias: Alias) : AliasAction(alias)
}
