package io.simplelogin.android.utils.extension

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun BottomSheetBehavior<View>.isExpanded() : Boolean =
    this.state == BottomSheetBehavior.STATE_EXPANDED

fun BottomSheetBehavior<View>.isHidden() : Boolean =
    this.state == BottomSheetBehavior.STATE_HIDDEN

fun BottomSheetBehavior<View>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<View>.hide()  {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}
