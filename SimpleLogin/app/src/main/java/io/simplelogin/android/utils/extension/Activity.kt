package io.simplelogin.android.utils.extension

import android.app.Activity
import android.content.*
import android.content.pm.LabeledIntent
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.utils.interfaces.Reversable
import io.simplelogin.android.utils.model.Alias
import io.simplelogin.android.utils.model.AliasMailbox
import io.simplelogin.android.utils.model.Mailbox

fun Activity.showKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this.currentFocus, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.dismissKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }
}

fun Activity.copyToClipboard(label: String, text: String): Boolean {
    val clipboardManager =
        (getSystemService(Context.CLIPBOARD_SERVICE) ?: false) as ClipboardManager
    val clipData = ClipData.newPlainText(label, text)
    clipboardManager.setPrimaryClip(clipData) // using setter will cause error 'val cannot be reassigned'
    return true
}

fun Activity.startSendEmailIntent(emailAddress: String) {
    val mailToIntent = Intent(Intent.ACTION_SENDTO)
    mailToIntent.data = Uri.parse("mailto:")
    mailToIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
    getIntentChooser(mailToIntent, "Send email using", listOf(packageName))?.let {
        startActivity(it)
    }
}

fun Activity.getIntentChooser(intent: Intent, chooserTitle: CharSequence? = null, filteredPackageNames: List<String>): Intent? {
    val resolveInfos = packageManager.queryIntentActivities(intent, 0)
    val excludedComponentNames = HashSet<ComponentName>()
    resolveInfos.forEach {
        val activityInfo = it.activityInfo
        val componentName = ComponentName(activityInfo.packageName, activityInfo.name)
        if (filteredPackageNames.contains(componentName.packageName))
            excludedComponentNames.add(componentName)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Intent.createChooser(intent, chooserTitle).putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, excludedComponentNames.toTypedArray())
    }
    if (resolveInfos.isNotEmpty()) {
        val targetIntents: MutableList<Intent> = ArrayList()
        for (resolveInfo in resolveInfos) {
            val activityInfo = resolveInfo.activityInfo
            if (excludedComponentNames.contains(ComponentName(activityInfo.packageName, activityInfo.name)))
                continue
            val targetIntent = Intent(intent)
            targetIntent.setPackage(activityInfo.packageName)
            targetIntent.component = ComponentName(activityInfo.packageName, activityInfo.name)
            // wrap with LabeledIntent to show correct name and icon
            val labeledIntent = LabeledIntent(targetIntent, activityInfo.packageName, resolveInfo.labelRes, resolveInfo.icon)
            // add filtered intent to a list
            targetIntents.add(labeledIntent)
        }
        val chooserIntent: Intent?
        // deal with M list separate problem
        chooserIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // create chooser with empty intent in M could fix the empty cells problem
            Intent.createChooser(Intent(), chooserTitle)
        } else {
            // create chooser with one target intent below M
            Intent.createChooser(targetIntents.removeAt(0), chooserTitle)
        }
        if (chooserIntent == null) {
            return null
        }
        // add initial intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toTypedArray<Parcelable>())
        return chooserIntent
    }
    return null
}

fun Activity.getScreenHeight(): Int {
    val size = Point()
    windowManager.defaultDisplay.getRealSize(size)
    // y is height, x is width
    return size.y
}

fun Activity.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}

fun Activity.showSelectMailboxesAlert(
    mailboxes: List<Mailbox>,
    selectedMailboxes: List<AliasMailbox>,
    save: (checkedMailboxes: List<AliasMailbox>) -> Unit
) {
    val items = arrayOf("[Select all]") + mailboxes.map { it.email }.toTypedArray()
    val checkedItems = BooleanArray(items.size)
    val selectedMailboxesName = selectedMailboxes.map { it.email }
    checkedItems.forEachIndexed { index, _ ->
        if (selectedMailboxesName.contains(items[index])) {
            checkedItems[index] = true
        }
    }

    MaterialAlertDialogBuilder(this)
        .setTitle("Select mailboxes")
        .setMultiChoiceItems(items, checkedItems) { dialog, which, _ ->
            val listView = (dialog as AlertDialog).listView
            // Select all
            if (which == 0) {
                checkedItems.forEachIndexed { index, _ ->
                    checkedItems[index] = true
                    listView.setItemChecked(index, true)
                }

                checkedItems[0] = false
                listView.setItemChecked(0, false)
            }

            // At least 1 mailbox is selected
            if (checkedItems.none { it }) {
                checkedItems[which] = true
                listView.setItemChecked(which, true)
            }
        }
        .setPositiveButton("Save") { _, _ ->
            val aliasMailboxes = mutableListOf<AliasMailbox>()
            checkedItems.forEachIndexed { index, isChecked ->
                if (isChecked) {
                    aliasMailboxes.add(mailboxes.first { it.email == items[index] }
                        .toAliasMailbox())
                }
            }

            save(aliasMailboxes)
        }
        .setNeutralButton("Cancel", null)
        .show()
}

fun Activity.alertReversableOptions(reversable: Reversable, alias: Alias? = null) {
    fun copyToClipboardAndToast(text: String) {
        copyToClipboard(text, text)
        toastShortly("Copied $text")
    }
    val toString = "Email to \"${reversable.email}\""
    val fromString = if (alias != null) " from \"${alias.email}\"" else ""

    MaterialAlertDialogBuilder(this, R.style.SlAlertDialogTheme)
        .setTitle(toString + fromString)
        .setItems(
            arrayOf(
                getString(R.string.copy_reverse_alias_with_display_name),
                getString(R.string.copy_reverse_alias_without_display_name),
                getString(R.string.begin_composing_with_default_email)
            )
        ) { _, itemIndex ->
            when (itemIndex) {
                0 -> copyToClipboardAndToast(reversable.reverseAlias)
                1 -> copyToClipboardAndToast(reversable.reverseAliasAddress)
                2 -> startSendEmailIntent(reversable.reverseAlias)
            }
        }
        .show()
}