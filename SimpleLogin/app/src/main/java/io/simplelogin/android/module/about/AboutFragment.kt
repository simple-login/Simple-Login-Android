package io.simplelogin.android.module.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.R
import io.simplelogin.android.databinding.FragmentAboutBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment
import io.simplelogin.android.utils.extension.getVersionName
import io.simplelogin.android.utils.extension.startSendEmailIntent

class AboutFragment : BaseFragment(), HomeActivity.OnBackPressed {
    companion object {
        const val OPEN_FROM_LOGIN_ACTIVITY = "openFromLoginActivity"
    }

    private lateinit var binding: FragmentAboutBinding
    private var openFromLoginActivity: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Determine if this is opened from LoginActivity or HomeActivity
        openFromLoginActivity =
            findNavController().graph.arguments.getValue(OPEN_FROM_LOGIN_ACTIVITY).defaultValue as Boolean

        binding = FragmentAboutBinding.inflate(inflater)

        when (openFromLoginActivity) {
            true -> {
                binding.toolbar.setNavigationIcon(R.drawable.ic_close_36dp)
                firebaseAnalytics.logEvent("open_about_fragment_from_login", null)
            }

            false -> firebaseAnalytics.logEvent("open_about_fragment_from_home", null)
        }

        binding.toolbar.setNavigationOnClickListener { finishOrNavigateUp() }

        binding.appVersionTextView.text = "SimpleLogin v${context?.getVersionName()}"
        val baseUrl = "https://simplelogin.io"

        binding.root.findViewById<View>(R.id.howTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToHowItWorksFragment()
            )
        }

        binding.root.findViewById<View>(R.id.securityTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/security")
            )
            firebaseAnalytics.logEvent("about_view_security", null)
        }

        binding.root.findViewById<View>(R.id.contactTextView).setOnClickListener {
            activity?.startSendEmailIntent("hi@simplelogin.io")
            firebaseAnalytics.logEvent("about_compose_email", null)
        }

        binding.root.findViewById<View>(R.id.faqTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToFaqFragment()
            )
        }

        binding.root.findViewById<View>(R.id.teamTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/about")
            )
            firebaseAnalytics.logEvent("about_view_team", null)
        }

        binding.root.findViewById<View>(R.id.pricingTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/pricing")
            )
            firebaseAnalytics.logEvent("about_view_pricing", null)
        }

        binding.root.findViewById<View>(R.id.blogTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/blog")
            )
            firebaseAnalytics.logEvent("about_view_blog", null)
        }

        binding.root.findViewById<View>(R.id.termsTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/terms")
            )
            firebaseAnalytics.logEvent("about_view_terms", null)
        }

        binding.root.findViewById<View>(R.id.privacyTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/privacy")
            )
            firebaseAnalytics.logEvent("about_view_privacy", null)
        }

        firebaseAnalytics.logEvent("open_about_fragment", null)
        return binding.root
    }

    private fun finishOrNavigateUp() {
        if (openFromLoginActivity) {
            activity?.finish()
        } else {
            showLeftMenu()
        }
    }

    // HomeActivity.OnBackPressed
    override fun onBackPressed() = finishOrNavigateUp()
}