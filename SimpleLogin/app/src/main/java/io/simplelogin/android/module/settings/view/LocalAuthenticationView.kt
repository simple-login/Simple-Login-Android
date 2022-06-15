package io.simplelogin.android.module.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import io.simplelogin.android.databinding.LayoutLocalAuthenticationBinding

class LocalAuthenticationView : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding =
        LayoutLocalAuthenticationBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun bind(isChecked: Boolean) {
        binding.biometricSwitch.isChecked = isChecked
    }

    fun setOnSwitchChangedListener(listener: (isChecked: Boolean) -> Unit) {
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked -> listener(isChecked) }
    }
}