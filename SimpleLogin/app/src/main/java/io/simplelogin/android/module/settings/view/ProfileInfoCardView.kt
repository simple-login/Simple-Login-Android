package io.simplelogin.android.module.settings.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import io.simplelogin.android.R
import io.simplelogin.android.databinding.LayoutProfileInfoCardViewBinding
import io.simplelogin.android.utils.model.UserInfo

class ProfileInfoCardView : RelativeLayout {
    // Initializer
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding =
        LayoutProfileInfoCardViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        background = ContextCompat.getDrawable(context, android.R.color.transparent)
    }

    // Functions
    fun bind(userInfo: UserInfo) {
        binding.displayNameTextView.text = userInfo.name
        binding.emailTextView.text = userInfo.email
        binding.avatarView.setAvatar(userInfo.profilePhotoUrl)
        
        val subscriptionStatus: String
        val subscriptionStatusColorResId: Int
        when {
            userInfo.inTrial -> {
                subscriptionStatus = "Premium trial membership"
                subscriptionStatusColorResId = android.R.color.holo_blue_light
                binding.upgradeTextView.visibility = View.VISIBLE
            }

            userInfo.isPremium -> {
                subscriptionStatus = "Premium membership"
                subscriptionStatusColorResId = R.color.colorPremium
                binding.upgradeTextView.visibility = View.GONE
            }

            else -> {
                subscriptionStatus = "Free membership"
                subscriptionStatusColorResId = R.color.colorDarkGray
                binding.upgradeTextView.visibility = View.VISIBLE
            }
        }

        binding.membershipTextView.text = subscriptionStatus
        binding.membershipTextView.setTextColor(ContextCompat.getColor(context, subscriptionStatusColorResId))
    }

    fun setOnUpgradeClickListener(listener: () -> Unit) {
        binding.upgradeTextView.setOnClickListener { listener() }
    }

    fun setOnModifyClickListener(listener: () -> Unit) {
        binding.modifyTextView.setOnClickListener { listener() }
    }
}