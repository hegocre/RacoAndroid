/**
 *  HTML Text Composable
 *  Adapted from https://github.com/ch4rl3x/HtmlText
 *  under the Apache 2.0 License
 */
package com.yara.raco.ui.components

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.core.text.getSpans
import java.lang.Integer.min

/**
 * Simple Text composable to show the text with html styling from a String.
 * Supported are:
 *
 * &lt;b>Bold&lt;/b>
 *
 * &lt;i>Italic&lt;/i>
 *
 * &lt;u>Underlined&lt;/u>
 *
 * &lt;strike>Strikethrough&lt;/strike>
 *
 * &lt;a href="https://google.de">Link&lt;/a>
 *
 * @see androidx.compose.material3.Text
 *
 */
@Composable
fun HtmlText(
    modifier: Modifier = Modifier,
    text: String,
    urlSpanStyle: SpanStyle = SpanStyle(
        color = MaterialTheme.colorScheme.secondary,
        textDecoration = TextDecoration.Underline
    ),
    colorMapping: Map<Color, Color> = emptyMap(),
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    val annotatedString = remember {
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            .toAnnotatedString(urlSpanStyle, colorMapping)
    }

    val clickableElements = annotatedString.getStringAnnotations(0, annotatedString.length - 1)

    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val boxPosX = remember { mutableStateListOf<Pair<Float, Float>>() }
    val boxPosY = remember { mutableStateListOf<Pair<Float, Float>>() }

    Box {
        Text(
            modifier = modifier,
            text = annotatedString,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            inlineContent = inlineContent,
            onTextLayout = {
                clickableElements.forEach { url ->
                    val startLine = it.getLineForOffset(url.start)
                    val endLine = it.getLineForOffset(url.end - 1)
                    if (startLine < endLine) {
                        boxPosX.add(
                            Pair(
                                it.getBoundingBox(it.getLineStart(startLine)).left,
                                it.getBoundingBox(it.getLineEnd(startLine) - 1).right
                            )
                        )
                    } else {
                        boxPosX.add(
                            Pair(
                                it.getBoundingBox(url.start).left,
                                it.getBoundingBox(url.end - 1).right
                            )
                        )
                    }
                    boxPosY.add(
                        Pair(
                            it.getBoundingBox(url.start).top,
                            it.getBoundingBox(url.end - 1).bottom
                        )
                    )
                }
                layoutResult.value = it
                onTextLayout(it)
            },
            style = style
        )

        for (i in 0 until min(boxPosX.size, boxPosY.size)) {
            val buttonPaddingX = with(LocalDensity.current) { boxPosX[i].first.toDp() }
            val buttonPaddingY = with(LocalDensity.current) { boxPosY[i].first.toDp() }
            val buttonWidth =
                with(LocalDensity.current) { (boxPosX[i].second - boxPosX[i].first).toDp() }
            val buttonHeight =
                with(LocalDensity.current) { (boxPosY[i].second - boxPosY[i].first).toDp() }
            Box(
                modifier = Modifier
                    .padding(start = buttonPaddingX, top = buttonPaddingY)
                    .width(buttonWidth)
                    .height(buttonHeight)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            layoutResult.value?.let { layoutResult ->
                                val position =
                                    layoutResult.getOffsetForPosition(offset.copy(y = offset.y + boxPosY[i].first))
                                annotatedString
                                    .getStringAnnotations(position, position)
                                    .firstOrNull()
                                    ?.let { sa ->
                                        if (sa.tag == "url") {
                                            uriHandler.openUri(sa.item)
                                        }
                                    }
                            }
                        }
                    }
            )
        }
    }


}

fun Spanned.toAnnotatedString(
    urlSpanStyle: SpanStyle = SpanStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline
    ),
    colorMapping: Map<Color, Color> = emptyMap()
): AnnotatedString {
    return buildAnnotatedString {
        append(this@toAnnotatedString.toString())
        val urlSpans = getSpans<URLSpan>()
        val styleSpans = getSpans<StyleSpan>()
        val colorSpans = getSpans<ForegroundColorSpan>()
        val underlineSpans = getSpans<UnderlineSpan>()
        val strikethroughSpans = getSpans<StrikethroughSpan>()
        urlSpans.forEach { urlSpan ->
            val start = getSpanStart(urlSpan)
            val end = getSpanEnd(urlSpan)
            addStyle(urlSpanStyle, start, end)
            addStringAnnotation("url", urlSpan.url, start, end) // NON-NLS
        }
        colorSpans.forEach { colorSpan ->
            val start = getSpanStart(colorSpan)
            val end = getSpanEnd(colorSpan)
            addStyle(SpanStyle(color = colorMapping.getOrElse(Color(colorSpan.foregroundColor)) {
                Color(
                    colorSpan.foregroundColor
                )
            }), start, end)
        }
        styleSpans.forEach { styleSpan ->
            val start = getSpanStart(styleSpan)
            val end = getSpanEnd(styleSpan)
            when (styleSpan.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ), start, end
                )
            }
        }
        underlineSpans.forEach { underlineSpan ->
            val start = getSpanStart(underlineSpan)
            val end = getSpanEnd(underlineSpan)
            addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
        }
        strikethroughSpans.forEach { strikethroughSpan ->
            val start = getSpanStart(strikethroughSpan)
            val end = getSpanEnd(strikethroughSpan)
            addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, end)
        }
    }
}
