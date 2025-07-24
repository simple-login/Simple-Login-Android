package io.simplelogin.android.ui.home.dialog

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import io.simplelogin.android.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class FullScreenMode {
    TEXT, QR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(
    alias: Alias,
    onDismiss: () -> Unit
) {
    var mode by rememberSaveable { mutableStateOf(FullScreenMode.TEXT) }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            mode = when (mode) {
                                FullScreenMode.TEXT -> FullScreenMode.QR
                                FullScreenMode.QR -> FullScreenMode.TEXT
                            }
                        }) {
                            Icon(
                                imageVector = when (mode) {
                                    FullScreenMode.TEXT -> Icons.Default.QrCode
                                    FullScreenMode.QR -> Icons.Default.Abc
                                },
                                contentDescription = when (mode) {
                                    FullScreenMode.TEXT -> stringResource(R.string.full_screen_qr_code_mode)
                                    FullScreenMode.QR -> stringResource(R.string.full_screen_text_mode)
                                }
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            var widthPx by remember { mutableIntStateOf(0) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .onGloballyPositioned { coordinates ->
                        widthPx = coordinates.size.width
                    },
                contentAlignment = Alignment.Center
            ) {
                val bitmap by produceState<Bitmap?>(initialValue = null, key1 = alias) {
                    value = withContext(Dispatchers.Default) {
                        generateQrCode(text = alias.email, size = widthPx * 2 / 3)
                    }
                }

                when (mode) {
                    FullScreenMode.TEXT -> FullScreenEmail(email = alias.email)

                    FullScreenMode.QR ->
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun FullScreenEmail(email: String) {
    var sliderPosition by rememberSaveable { mutableFloatStateOf(0.75f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.regular),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentSize(Alignment.Center)
        ) {
            Text(
                text = email,
                fontWeight = FontWeight.Bold,
                fontSize = (sliderPosition * 60).sp,
                lineHeight = (sliderPosition * 75).sp
            )
        }

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0.5f..1f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.regular)
        )
    }
}

private fun generateQrCode(
    text: String,
    size: Int,
    margin: Int = 1
): Bitmap {
    val hints = mapOf(
        EncodeHintType.MARGIN to margin,
        EncodeHintType.CHARACTER_SET to "UTF-8"
    )

    val bitMatrix: BitMatrix = MultiFormatWriter()
        .encode(text, BarcodeFormat.QR_CODE, size, size, hints)

    val bmp = createBitmap(size, size)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
        }
    }
    return bmp
}