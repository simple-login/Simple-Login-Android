package io.simplelogin.android.module.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

        binding.upgradeTextView.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToPremiumFragment()
            )
        }

        when {
            userInfo.inTrial -> {
                binding.membershipTextView.text = "Premium trial membership"
                binding.membershipTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.holo_blue_light
                    )
                )
                binding.upgradeTextView.visibility = View.VISIBLE
            }

            userInfo.isPremium -> {
                binding.membershipTextView.text = "Premium membership"
                binding.membershipTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorPremium
                    )
                )
                binding.upgradeTextView.visibility = View.GONE
            }

            else -> {
                binding.membershipTextView.text = "Free membership"
                binding.membershipTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorDarkGray
                    )
                )
                binding.upgradeTextView.visibility = View.VISIBLE
            }
        }

        firebaseAnalytics.logEvent("open_settings_fragment", null)
        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        showLeftMenu()
    }
}