package io.simplelogin.android.ui.util

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

fun WindowSizeClass.isTwoPaneEligible(): Boolean =
    isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)