package io.simplelogin.android.module.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentSettingsBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.model.UserInfo

class SettingsFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentSettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        val userInfo =
            findNavController().graph.arguments.getValue(HomeActivity.USER_INFO).defaultValue as UserInfo

        binding.usernameTextView.text = userInfo.name
        binding.emailTextView.text = userInfo.email

        if (userInfo.inTrial) {
            binding.membershipTextView.text = "Premium trial membership"
            binding.membershipTextView.setTextColor(requireContext().getColor(android.R.color.holo_blue_light))
        } else if (userInfo.isPremium) {
            binding.membershipTextView.text = "Premium membership"
            binding.membershipTextView.setTextColor(requireContext().getColor(R.color.colorPremium))
        } else {
            binding.membershipTextView.text = "Free membership"
            binding.membershipTextView.setTextColor(requireContext().getColor(R.color.colorDarkGray))
        }

        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        showLeftMenu()
    }
}