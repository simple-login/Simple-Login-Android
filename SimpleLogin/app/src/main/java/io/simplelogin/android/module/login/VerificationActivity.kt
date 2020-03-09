package io.simplelogin.android.module.login

import android.os.Bundle
import io.simplelogin.android.databinding.ActivityVerificationBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class VerificationActivity : BaseAppCompatActivity() {
    companion object {
        const val API_KEY = "apiKey"
        const val MFA_KEY = "mfaKey"
    }
    private lateinit var binding: ActivityVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mfaKey = intent.getStringExtra(MFA_KEY)
        mfaKey?.let {
            binding.textView.text = mfaKey
        }
    }
}