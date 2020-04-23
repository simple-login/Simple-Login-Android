package io.simplelogin.android.module.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentPremiumBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.startSendEmailIntent

class PremiumFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentPremiumBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPremiumBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.upgradeButton.setOnClickListener {
            findNavController().navigate(
                PremiumFragmentDirections.actionPremiumFragmentToWebViewFragment2(
                    "https://app.simplelogin.io/dashboard/pricing"
                )
            )
            firebaseAnalytics.logEvent("premium_upgrade", null)
        }

        binding.termsTextView.setOnClickListener {
            findNavController().navigate(
                PremiumFragmentDirections.actionPremiumFragmentToWebViewFragment2(
                    "https://simplelogin.io/terms/"
                )
            )
            firebaseAnalytics.logEvent("premium_view_terms", null)
        }

        binding.privacyTextView.setOnClickListener {
            findNavController().navigate(
                PremiumFragmentDirections.actionPremiumFragmentToWebViewFragment2(
                    "https://simplelogin.io/privacy/"
                )
            )
            firebaseAnalytics.logEvent("premium_view_privacy", null)
        }

        binding.contactButton.setOnClickListener {
            activity?.startSendEmailIntent("hi@simplelogin.io")
            firebaseAnalytics.logEvent("premium_compose_email", null)
        }

        return binding.root
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}