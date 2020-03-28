package io.simplelogin.android.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.simplelogin.android.R

abstract class SwipeToDeleteCallback(private val context: Context) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.LEFT
) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_24dp)
    private val deleteIconIntrinsicHeight = deleteIcon?.intrinsicHeight ?: 24
    private val deleteIconIntrinsicWidth = deleteIcon?.intrinsicWidth ?: 24
    private val background = ColorDrawable()
    private val backgroundColor = ContextCompat.getColor(context, R.color.colorNegative)
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        /**
         * To disable "swipe" for specific item return 0 here.
         * For example:
         * if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
         * if (viewHolder?.adapterPosition == 0) return 0
         */
        return super.getMovementFlags(recyclerView, viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Don't support moving items UP or DOWN
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCancelled = dX == 0f && !isCurrentlyActive

        if (isCancelled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw red background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        deleteIcon?.setTint(ContextCompat.getColor(context, android.R.color.white))
        val deleteIconTop = itemView.top + (itemHeight - deleteIconIntrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - deleteIconIntrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - deleteIconIntrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = itemView.bottom - deleteIconMargin

        // Draw delete icon
        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        canvas?.drawRect(left, top, right, bottom, clearPaint)
    }
}