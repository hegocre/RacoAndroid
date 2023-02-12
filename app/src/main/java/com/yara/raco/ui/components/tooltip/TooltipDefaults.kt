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

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.SecureFlagPolicy
import kotlin.time.Duration.Companion.milliseconds

// Dimension values taken from framework default dimensions:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/values/dimens.xml#724
internal val TooltipYOffsetTouch = 16.dp
internal val TooltipYOffsetNonTouch = 0.dp
internal val TooltipMargin = PaddingValues(all = 8.dp)
internal val TooltipPadding = PaddingValues(horizontal = 16.dp, vertical = 6.5.dp)
internal val TooltipShape = RoundedCornerShape(2.dp)
internal val TooltipPreciseAnchorThreshold = 96.dp
internal val TooltipPreciseAnchorExtraThreshold = 8.dp

// Text style has been extracted from the framework TextAppearance.Tooltip:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/values/styles.xml#973
internal val TooltipTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontSize = 14.sp,
)

// Background colors extracted from framework core colors:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/values/colors.xml#220
internal val TooltipBackgroundDark = Color(0xe6616161)
internal val TooltipBackgroundLight = Color(0xe6FFFFFF)

// Foreground colors in framework Theme.Material(.Light):
// https://android.googlesource.com/platform/frameworks/base/+/863a88ef074b8bda119e7f8645e5a53f7d51fc34/core/res/res/values/themes_material.xml#410
// https://android.googlesource.com/platform/frameworks/base/+/863a88ef074b8bda119e7f8645e5a53f7d51fc34/core/res/res/values/themes_material.xml#786
internal val TooltipForegroundDark = Color.White
internal val TooltipForegroundLight = Color.Black

// Background colors extracted from framework core colors:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/values/colors.xml#220

// Tooltip duration values via ViewConfiguration are only accessible through internal calls, so we copy them:
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/java/android/view/ViewConfiguration.java#278
internal val TooltipLongPressHideTimeout = 1500.milliseconds
internal val TooltipHoverShowTimeout = 500.milliseconds
internal val TooltipHoverHideTimeout = 15000.milliseconds

// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/values/config.xml#172
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/anim/tooltip_enter.xml#21
// https://android.googlesource.com/platform/frameworks/base/+/02772f2e7a6f497a6e209bb8104681468d40d090/core/res/res/anim/tooltip_exit.xml#21
internal val TooltipAlphaAnimationInterpolator = AccelerateDecelerateInterpolator()
internal val TooltipAlphaAnimationSpec: AnimationSpec<Float> = tween(
    durationMillis = 150,
    easing = { x -> TooltipAlphaAnimationInterpolator.getInterpolation(x) },
)

// Custom properties which match the default tooltip popup as best as possible
internal val TooltipPopupProperties = PopupProperties(
    focusable = false,
    dismissOnBackPress = false,
    dismissOnClickOutside = true,
    securePolicy = SecureFlagPolicy.Inherit,
    excludeFromSystemGesture = true,
    clippingEnabled = true,
)
