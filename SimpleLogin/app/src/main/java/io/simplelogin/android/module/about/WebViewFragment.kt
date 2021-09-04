package io.simplelogin.android.module.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import io.simplelogin.android.databinding.FragmentWebviewBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.baseclass.BaseFragment

class WebViewFragment : BaseFragment(), HomeActivity.OnBackPressed {
    private lateinit var binding: FragmentWebviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebviewBinding.inflate(inflater)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val url = WebViewFragmentArgs.fromBundle(requireArguments()).url
        binding.webView.loadUrl(url)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.toolbarTitleText.text = view?.title
            }
        }

        return binding.root
    }

    override fun onBackPressed() {
        findNavController().navigateUp()
    }
}
