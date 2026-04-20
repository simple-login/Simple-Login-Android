package io.simplelogin.core.designsystem

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class IconPosition { LEADING, TRAILING }

@Composable
fun TextWithInlineIcon(
    text: String,
    textColor: Color,
    icon: Painter,
    modifier: Modifier = Modifier,
    iconSize: TextUnit = 16.sp,
    iconTint: Color = LocalContentColor.current,
    iconPosition: IconPosition = IconPosition.LEADING,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val iconId = "icon_id"
    val annotatedText = buildAnnotatedString {
        if (iconPosition == IconPosition.LEADING) {
            appendInlineContent(iconId, "[icon]")
            append("\u00A0") // Non-breaking space
        }

        append(text)

        if (iconPosition == IconPosition.TRAILING) {
            append("\u00A0") // Non-breaking space
            appendInlineContent(iconId, "[icon]")
        }
    }

    val inlineContent = mapOf(
        iconId to InlineTextContent(
            placeholder = Placeholder(
                width = iconSize,
                height = iconSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .width(iconSize.value.dp)
                    .height(iconSize.value.dp)
            )
        }
    )

    Text(
        text = annotatedText,
        fontWeight = fontWeight,
        inlineContent = inlineContent,
        modifier = modifier,
        style = style,
        color = textColor
    )
}