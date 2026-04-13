package io.simplelogin.android.core.common.usecase

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface CopyToClipboardUseCase {
    suspend operator fun invoke(label: String, content: String)
}

class CopyToClipboardUseCaseImpl @Inject constructor(@ApplicationContext private val context: Context) :
    CopyToClipboardUseCase {
    override suspend fun invoke(label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
    }
}