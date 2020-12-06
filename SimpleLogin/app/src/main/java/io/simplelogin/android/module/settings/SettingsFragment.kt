package io.simplelogin.android.module.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentSettingsBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.model.UserInfo


class SettingsFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentSettingsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { showLeftMenu() }

        val userInfo =
            findNavController().graph.arguments.getValue(HomeActivity.USER_INFO).defaultValue as UserInfo

        binding.usernameTextView.text = userInfo.name
        binding.emailTextView.text = userInfo.email
        binding.darkModeSwitch.isChecked = SLSharedPreferences.getShouldForceDarkMode(requireContext())

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

        binding.darkModeSwitch.setOnCheckedChangeListener { _, shouldForceDarkMode ->
            this.context?.let { SLSharedPreferences.setShouldForceDarkMode(it, shouldForceDarkMode) }

            if (shouldForceDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }

            val intent: Intent? = context?.packageName?.let {
                context?.packageManager
                    ?.getLaunchIntentForPackage(it)
            }
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
        }

        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        showLeftMenu()
    }
}
