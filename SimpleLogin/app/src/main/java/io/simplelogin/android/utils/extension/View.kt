package io.simplelogin.android.utils.extension

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import io.simplelogin.android.R

@Suppress("MagicNumber")
fun View.customSetEnabled(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1.0f else 0.5f
}

fun View.makeSubviewsClippedToBound() {
    outlineProvider = ViewOutlineProvider.BACKGROUND
    clipToOutline = true
}

fun View.shake() = startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))

@Suppress("MagicNumber")
fun View.fadeOut() {
    val valueAnimator = ValueAnimator.ofFloat(1f, 0f)
    valueAnimator.addUpdateListener { alpha = it.animatedValue as Float }
    valueAnimator.duration = 200
    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.start()
}
