package io.simplelogin.android.module.settings.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.simplelogin.android.R
import io.simplelogin.android.databinding.LayoutAvatarViewBinding

class AvatarView : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding =
        LayoutAvatarViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun setAvatar(urlString: String?) {
        if (urlString != null) {
            Glide.with(this).load(Uri.parse(urlString)).into(binding.imageView)
            binding.cardView.foreground = null
        } else {
            binding.imageView.setImageResource(R.drawable.ic_user_48dp)
            binding.cardView.foreground = ContextCompat.getDrawable(context, R.drawable.shape_oval_outline_primary_color)
        }
    }
}