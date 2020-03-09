package io.simplelogin.android.module.login

import android.app.Activity
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

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        val mfaKey = intent.getStringExtra(MFA_KEY)

    }

    override fun onBackPressed() = Unit
}