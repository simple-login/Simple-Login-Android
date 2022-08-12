package io.simplelogin.android.module.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import io.simplelogin.android.R
import io.simplelogin.android.databinding.LayoutConnectWithProtonViewBinding

class ConnectWithProtonView: RelativeLayout {
    // Initializer
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding =
        LayoutConnectWithProtonViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    fun bind(connectedProtonAddress: String?) {
        if (connectedProtonAddress != null) {
            binding.connectWithProtonButton.visibility = View.GONE
            binding.unlinkProtonAccountButton.visibility = View.VISIBLE
            binding.connectWithProtonInfoText.visibility = View.GONE
            binding.accountConnectedWithProtonText.visibility = View.VISIBLE
            binding.accountConnectedWithProtonText.text = context.getString(R.string.currently_linked_proton_account, connectedProtonAddress)
        } else {
            binding.connectWithProtonButton.visibility = View.VISIBLE
            binding.unlinkProtonAccountButton.visibility = View.GONE
            binding.connectWithProtonInfoText.visibility = View.VISIBLE
            binding.accountConnectedWithProtonText.visibility = View.GONE
        }
    }

    fun setOnConnectButtonClickListener(listener: () -> Unit) {
        binding.connectWithProtonButton.setOnClickListener {
            listener()
        }
    }

    fun setOnUnlinkButtonClickListener(listener: () -> Unit) {
        binding.unlinkProtonAccountButton.setOnClickListener {
            listener()
        }
    }
}