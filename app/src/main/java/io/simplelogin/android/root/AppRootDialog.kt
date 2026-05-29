package io.simplelogin.android.root

sealed interface AppRootDialog {
    data object LogOut : AppRootDialog
}
