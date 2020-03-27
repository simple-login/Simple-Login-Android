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

        if (openFromLoginActivity) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_close_36dp)
        }

        binding.toolbar.setNavigationOnClickListener { finishOrNavigateUp() }

        binding.appVersionTextView.text = "SimpleLogin v${context?.getVersionName()}"
        binding.howTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToHowItWorksFragment()
            )
        }

        binding.contactTextView.setOnClickListener {
            activity?.startSendEmailIntent("hi@simplelogin.io")
        }

        val base_url = "https://simplelogin.io"
        binding.faqTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToFaqFragment()
            )
        }
        binding.teamTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$base_url/about")
            )
        }

        binding.pricingTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$base_url/pricing")
            )
        }

        binding.blogTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$base_url/blog")
            )
        }

        binding.termsTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$base_url/terms")
            )
        }

        binding.privacyTextView.setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$base_url/privacy")
            )
        }

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