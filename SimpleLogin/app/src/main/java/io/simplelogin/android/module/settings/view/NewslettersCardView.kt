package io.simplelogin.android.module.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import io.simplelogin.android.databinding.LayoutNewslettersCardViewBinding

class NewslettersCardView : RelativeLayout {
    // Initializer
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding =
        LayoutNewslettersCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    // Functions
    fun bind(isChecked: Boolean) {
        binding.newslettersSwitch.isChecked = isChecked
    }

    fun setOnSwitchChangedListener(listener: (isChecked: Boolean) -> Unit) {
        binding.newslettersSwitch.setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }
}