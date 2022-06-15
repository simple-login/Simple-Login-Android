package io.simplelogin.android.utils.extension

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.EditText

fun EditText.placeCursorToEnd() = setSelection(text.length)

@SuppressLint("ClickableViewAccessibility")
fun EditText.onDrawableEndTouch(listener: () -> Unit) {
    // Drawable right: 2, left: 0, top: 1, bottom: 3
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= this.right - this.compoundDrawables[2].bounds.width()) {
                listener()
                return@setOnTouchListener true
            }
        }
        false
    }
}

/*fun EditText.setShowPassword(isShowing: Boolean) {
    val drawableEnd: Drawable
    val inputType: Int
    if (isShowing) {
        drawableEnd = ContextCompat.getDrawable(context, R.drawable.ic_eye_slash) ?: return
        inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        drawableEnd = ContextCompat.getDrawable(context, R.drawable.ic_eye_fill) ?: return
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    drawableEnd.setBounds(0, 0, drawableEnd.intrinsicWidth, drawableEnd.intrinsicHeight)
    setCompoundDrawables(null, null, drawableEnd, null)
    this.inputType = inputType
}*/

fun EditText.trim() {
    setText(text.toString().trim())
}