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
import io.simplelogin.android.utils.extension.openUrlInBrowser
import io.simplelogin.android.utils.extension.startSendEmailIntent

class AboutFragment : BaseFragment(), HomeActivity.OnBackPressed {
    companion object {
        const val OPEN_FROM_LOGIN_ACTIVITY = "openFromLoginActivity"
    }

    private lateinit var binding: FragmentAboutBinding
    private var openFromLoginActivity = true

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Determine if this is opened from LoginActivity or HomeActivity
        val arguments = findNavController().graph.arguments
        if (arguments.isNotEmpty()) {
            openFromLoginActivity = arguments.getValue(OPEN_FROM_LOGIN_ACTIVITY).defaultValue as Boolean
        }

        binding = FragmentAboutBinding.inflate(inflater)

        if (openFromLoginActivity) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_close_24dp)
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
        }

        binding.root.findViewById<View>(R.id.contactTextView).setOnClickListener {
            activity?.startSendEmailIntent("hi@simplelogin.io")
        }

        binding.root.findViewById<View>(R.id.whatTextView).setOnClickListener {
            findNavController().navigate(AboutFragmentDirections.actionAboutFragmentToWhatYouCanDoFragment())
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
        }

        binding.root.findViewById<View>(R.id.pricingTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/pricing")
            )
        }

        binding.root.findViewById<View>(R.id.blogTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/blog")
            )
        }

        binding.root.findViewById<View>(R.id.helpTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/help")
            )
        }

        binding.root.findViewById<View>(R.id.roadmapTextView).setOnClickListener {
            activity?.openUrlInBrowser("https://github.com/simple-login/app/projects/1")
        }

        binding.root.findViewById<View>(R.id.termsTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/terms")
            )
        }

        binding.root.findViewById<View>(R.id.privacyTextView).setOnClickListener {
            findNavController().navigate(
                AboutFragmentDirections.actionAboutFragmentToWebViewFragment("$baseUrl/privacy")
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
