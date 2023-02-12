/*
 * Copyright (C) 2006,2016 The Android Open Source Project
 * Copyright (C) 2022 Patrick Goldinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yara.raco.ui.components.tooltip

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.Window
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import com.yara.raco.ui.theme.isLight
import kotlinx.coroutines.*

/**
 * Material design tooltip following Android's framework tooltip design as close as possible by default.
 *
 * The tooltip anchors itself automatically to the element this modifier is applied and does automatic positioning and
 * sizing in a similar way to the Android framework tooltip.
 *
 * Both long-press touch interactions and mouse hover events are supported. For long-press touches, the tooltip starts
 * to show after the user has triggered a long-click and will then stay until 1.5 seconds after the touch up or cancel
 * event. For mouse hover interactions, the tooltip is shown after a minimum of 0.5 seconds hover, and then shows until
 * either the mouse stops hovering or 15 seconds have passed, whichever comes first.
 *
 * This modifier by default can only be used within an [Activity] or [InputMethodService] context, as it requires a
 * window to properly show. Alternatively a custom [windowResolver] can be passed, where the responsibility is up to
 * the caller to find the window reference.
 *
 * @param text The text to show in the tooltip, supports [AnnotatedString].
 * @param backgroundColor The background color of this tooltip. Defaults to #e6FFFFFF for dark and #e6616161 for light
 *  Material themes.
 * @param textColor The text color of this tooltip. Defaults to [Color.Black] for dark and [Color.White] for light
 *  Material themes.
 * @param textStyle The text style to be applied to the text. Defaults to 14sp sans-serif font.
 * @param overflow How overflow of the text should be handed. Defaults to [TextOverflow.Ellipsis].
 * @param maxLines How many lines the tooltip can have at most. Defaults to 3.
 * @param margin The margin of this tooltip. Defaults to 8dp on all edges.
 * @param padding The padding of the tooltip. Defaults to 16dp horizontally and 6.5dp vertically.
 * @param shape The shape of the tooltip. Defaults to a rounded corner shape with 2dp corner radius on all corners.
 * @param windowResolver The window resolver, which is responsible for retrieving the local window. The default
 *  implementation supports both [Activity] and [InputMethodService] contexts. If a custom provider is passed, it must
 *  not return null.
 *
 * @author Patrick Goldinger
 *
 * @since 0.1.0
 */
fun Modifier.tooltip(
    text: CharSequence,
    backgroundColor: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = TooltipTextStyle,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    maxLines: Int = 3,
    margin: PaddingValues = TooltipMargin,
    padding: PaddingValues = TooltipPadding,
    shape: Shape = TooltipShape,
    windowResolver: @Composable () -> Window = { LocalContext.current.findWindow()!! },
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "tooltip"
        properties["text"] = text
        properties["backgroundColor"] = backgroundColor
        properties["textColor"] = textColor
    }
) {
    val viewConfiguration = LocalViewConfiguration.current
    val scope = rememberCoroutineScope()

    val finalBackgroundColor = when {
        backgroundColor.isSpecified -> backgroundColor
        else -> if (MaterialTheme.colorScheme.isLight()) TooltipBackgroundDark else TooltipBackgroundLight
    }
    val finalTextColor = when {
        textColor.isSpecified -> textColor
        else -> if (MaterialTheme.colorScheme.isLight()) TooltipForegroundDark else TooltipForegroundLight
    }

    var isTooltipShowing by remember { mutableStateOf(false) }
    val tooltipAlpha by animateFloatAsState(
        targetValue = if (isTooltipShowing) 1f else 0f,
        animationSpec = TooltipAlphaAnimationSpec,
    )

    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    var anchorBounds by remember { mutableStateOf(Rect.Zero) }
    var lastTriggerEvent by remember { mutableStateOf<PointerEvent?>(null) }
    var tooltipSize by remember { mutableStateOf(Size.Zero) }
    val tooltipOffset = rememberComputedTooltipOffset(
        windowResolver,
        tooltipSize,
        anchorBounds,
        anchorEventPosition = lastTriggerEvent?.changes?.first()?.position ?: Offset.Zero,
        triggeredFromTouch = lastTriggerEvent?.type != PointerEventType.Enter,
    )

    if (tooltipAlpha > 0f) {
        Popup(
            properties = TooltipPopupProperties,
            alignment = Alignment.Center,
            offset = tooltipOffset.round(),
            onDismissRequest = { isTooltipShowing = false },
        ) {
            Box(
                modifier = Modifier
                    .onSizeChanged { tooltipSize = it.toSize() }
                    .alpha(tooltipAlpha)
                    .padding(margin)
                    .widthIn(max = 256.dp)
                    .wrapContentHeight()
                    .background(finalBackgroundColor, shape)
                    .padding(padding),
            ) {
                val annotatedText = remember(text) {
                    if (text is AnnotatedString) text else AnnotatedString(text.toString())
                }
                Text(
                    text = annotatedText,
                    color = finalTextColor,
                    overflow = overflow,
                    maxLines = maxLines,
                    style = textStyle,
                )
            }
        }
    }

    var hoverShowTimeoutJob: Job? = null
    var hoverHideTimeoutJob: Job? = null
    var longPressShowTimeoutJob: Job? = null
    var longPressHideTimeoutJob: Job? = null

    DisposableEffect(Unit) {
        onDispose {
            hoverShowTimeoutJob?.cancel()
            hoverHideTimeoutJob?.cancel()
            longPressShowTimeoutJob?.cancel()
            longPressHideTimeoutJob?.cancel()
            isTooltipShowing = false
        }
    }

    Modifier
        .onGloballyPositioned { coordinates ->
            anchorBounds = coordinates.boundsInRoot()
        }
        .pointerInput(Unit) {
            val currentContext = currentCoroutineContext()
            awaitPointerEventScope {
                while (currentContext.isActive) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    when (event.type) {
                        PointerEventType.Press -> {
                            if (!event.changes.all { it.changedToDown() }) continue
                            isPressed = true
                            if (!isHovered && !isTooltipShowing) {
                                lastTriggerEvent = event
                            }
                            longPressShowTimeoutJob?.cancel()
                            longPressHideTimeoutJob?.cancel()
                            longPressShowTimeoutJob = scope.launch {
                                delay(viewConfiguration.longPressTimeoutMillis)
                                isTooltipShowing = true
                            }
                        }
                        PointerEventType.Release -> {
                            isPressed = false
                            longPressShowTimeoutJob?.cancel()
                            if (isTooltipShowing) {
                                event.changes.forEach { it.consume() }
                                longPressHideTimeoutJob = scope.launch {
                                    delay(TooltipLongPressHideTimeout.inWholeMilliseconds)
                                    isTooltipShowing = false
                                }
                            }
                        }
                        PointerEventType.Enter -> {
                            isHovered = true
                            if (!isPressed && !isTooltipShowing) {
                                lastTriggerEvent = event
                            }
                            hoverShowTimeoutJob?.cancel()
                            hoverHideTimeoutJob?.cancel()
                            hoverShowTimeoutJob = scope.launch {
                                delay(TooltipHoverShowTimeout.inWholeMilliseconds)
                                isTooltipShowing = true
                                hoverHideTimeoutJob = scope.launch {
                                    delay(TooltipHoverHideTimeout.inWholeMilliseconds)
                                    isTooltipShowing = false
                                }
                            }
                        }
                        PointerEventType.Exit -> {
                            isHovered = false
                            hoverShowTimeoutJob?.cancel()
                            hoverHideTimeoutJob?.cancel()
                            if (!isPressed) {
                                isTooltipShowing = false
                            }
                        }
                    }
                }
            }
        }
}

private tailrec fun Context.findWindow(): Window? {
    return when (this) {
        is Activity -> window
        is InputMethodService -> window?.window
        is ContextWrapper -> baseContext.findWindow()
        else -> null
    }
}

// Method implementation idea based on:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/java/com/android/internal/view/TooltipPopup.java#94
@Composable
private fun rememberComputedTooltipOffset(
    windowResolver: @Composable () -> Window,
    tooltipSize: Size,
    anchorBounds: Rect,
    anchorEventPosition: Offset,
    triggeredFromTouch: Boolean,
): Offset = with(LocalDensity.current) {
    val rootView = LocalView.current
    val windowView = windowResolver().findViewById<View>(android.R.id.content)

    val tempPosRootView = remember { intArrayOf(0, 0) }
    val tempPosWindowView = remember { intArrayOf(0, 0) }

    return@with remember(
        windowView,
        tooltipSize,
        anchorBounds,
        anchorEventPosition,
        triggeredFromTouch
    ) {
        rootView.getLocationOnScreen(tempPosRootView)
        windowView.getLocationOnScreen(tempPosWindowView)
        val anchorOffset = Offset(
            x = anchorBounds.topLeft.x + tempPosRootView[0] - tempPosWindowView[0] + (anchorBounds.width / 2f),
            y = anchorBounds.topLeft.y + tempPosRootView[1] - tempPosWindowView[1] + (anchorBounds.height / 2f),
        )

        val edgeAbove: Float
        val edgeBelow: Float
        if (anchorBounds.height >= TooltipPreciseAnchorThreshold.toPx()) {
            edgeAbove = anchorEventPosition.y - TooltipPreciseAnchorExtraThreshold.toPx()
            edgeBelow = anchorEventPosition.y + TooltipPreciseAnchorExtraThreshold.toPx()
        } else {
            edgeAbove = (-anchorBounds.height / 2f)
            edgeBelow = anchorBounds.height / 2f
        }

        val baseYOffset =
            (if (triggeredFromTouch) TooltipYOffsetTouch else TooltipYOffsetNonTouch).toPx()
        val yAbove = edgeAbove - baseYOffset - (tooltipSize.height / 2f)
        val yBelow = edgeBelow + baseYOffset + (tooltipSize.height / 2f)
        val tooltipOffset = Offset(
            // We don't manually ensure that the x-offset is on-screen, Popup() is doing this for us automatically
            x = when {
                anchorBounds.width >= TooltipPreciseAnchorThreshold.toPx() -> anchorEventPosition.x - (anchorBounds.width / 2f)
                else -> 0f
            },
            // We manually ensure that the y-offset is on-screen, because Popup() would put the tooltip above the anchor
            // view in some edge cases, which is not something we want
            y = when {
                triggeredFromTouch -> if (anchorOffset.y + yAbove - (tooltipSize.height / 2f) >= 0) yAbove else yBelow
                else -> if (anchorOffset.y + yBelow + (tooltipSize.height / 2f) <= windowView.height) yBelow else yAbove
            },
        )

        return@remember tooltipOffset
    }
}
