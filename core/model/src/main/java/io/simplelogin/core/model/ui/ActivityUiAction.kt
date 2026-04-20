package io.simplelogin.core.model.ui

enum class ActivityUiAction {
    COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME,
    COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME,
    COPY_ADDRESS,
    OPEN_DEFAULT_EMAIL_CLIENT;

//    fun title(context: Context) = when (this) {
//        COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME ->
//            context.getString(R.string.copy_reverse_alias_with_display_name)
//
//        COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME ->
//            context.getString(R.string.copy_reverse_alias_without_display_name)
//
//        COPY_ADDRESS -> context.getString(R.string.copy_alias_address)
//        OPEN_DEFAULT_EMAIL_CLIENT -> context.getString(R.string.open_default_email_client)
//    }
//
//    val icon: ImageVector
//        get() = when (this) {
//            COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME,
//            COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME,
//            COPY_ADDRESS -> Icons.Default.ContentCopy
//
//            OPEN_DEFAULT_EMAIL_CLIENT -> Icons.Default.ContentCopy
//        }
}