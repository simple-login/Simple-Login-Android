package io.simplelogin.android.module.login

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.simplelogin.android.databinding.ActivityVerificationBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.VerificationMode

class VerificationActivity : BaseAppCompatActivity() {
    companion object {
        const val MFA_MODE = "mfaMode"
        const val ACCOUNT_ACTIVATION_MODE = "accountActivationMode"
        const val API_KEY = "apiKey"
    }
    private lateinit var binding: ActivityVerificationBinding
    private lateinit var verificationMode: VerificationMode
    private val dash = "-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        verificationMode = getVerificationMode()

        when (verificationMode) {
            is VerificationMode.Mfa -> binding.toolbarTitleText.text = "Enter OTP"
            is VerificationMode.AccountActivation -> binding.toolbarTitleText.text = "Enter activation code"
        }

        reset()
        setUpClickListeners()
    }

    private fun getVerificationMode() : VerificationMode {
        val mfa = intent.getParcelableExtra<VerificationMode.Mfa>(MFA_MODE)

        if (mfa != null) return mfa

        val accountActivation = intent.getParcelableExtra<VerificationMode.AccountActivation>(
            ACCOUNT_ACTIVATION_MODE)

        if (accountActivation != null) return accountActivation

        throw IllegalStateException("VerificatioMode not found in intent")
    }

    private fun setUpClickListeners() {
        binding.zeroButton.setOnClickListener { addNumber("0") }
        binding.oneButton.setOnClickListener { addNumber("1") }
        binding.twoButton.setOnClickListener { addNumber("2") }
        binding.threeButton.setOnClickListener { addNumber("3") }
        binding.fourButton.setOnClickListener { addNumber("4") }
        binding.fiveButton.setOnClickListener { addNumber("5") }
        binding.sixButton.setOnClickListener { addNumber("6") }
        binding.sevenButton.setOnClickListener { addNumber("7") }
        binding.eightButton.setOnClickListener { addNumber("8") }
        binding.nineButton.setOnClickListener { addNumber("9") }
        binding.cancelButton.setOnClickListener { finish() }
        binding.deleteButton.setOnClickListener { deleteLastNumber() }
    }

    private fun addNumber(number: String) {
        if (binding.firstNumberTextView.text == dash) {
            binding.firstNumberTextView.text = number
            showError(false)
        } else if (binding.secondNumberTextView.text == dash) {
            binding.secondNumberTextView.text = number
        } else if (binding.thirdNumberTextView.text == dash) {
            binding.thirdNumberTextView.text = number
        } else if (binding.fourthNumberTextView.text == dash) {
            binding.fourthNumberTextView.text = number
        } else if (binding.fifthNumberTextView.text == dash) {
            binding.fifthNumberTextView.text = number
        } else if (binding.sixthNumberTextView.text == dash) {
            binding.sixthNumberTextView.text = number
            var code = ""
            code += binding.firstNumberTextView.text
            code += binding.secondNumberTextView.text
            code += binding.thirdNumberTextView.text
            code += binding.fourthNumberTextView.text
            code += binding.fifthNumberTextView.text
            code += binding.sixthNumberTextView.text

            verify(code)
        }
    }

    private fun deleteLastNumber() {
        if (binding.sixthNumberTextView.text != dash) {
            binding.sixthNumberTextView.text = dash
        } else if (binding.fifthNumberTextView.text != dash) {
            binding.fifthNumberTextView.text = dash
        } else if (binding.fourthNumberTextView.text != dash) {
            binding.fourthNumberTextView.text = dash
        } else if (binding.thirdNumberTextView.text != dash) {
            binding.thirdNumberTextView.text = dash
        } else if (binding.secondNumberTextView.text != dash) {
            binding.secondNumberTextView.text = dash
        } else if (binding.firstNumberTextView.text != dash) {
            binding.firstNumberTextView.text = dash
        }
    }

    private fun reset() {
        binding.firstNumberTextView.text = dash
        binding.secondNumberTextView.text = dash
        binding.thirdNumberTextView.text = dash
        binding.fourthNumberTextView.text = dash
        binding.fifthNumberTextView.text = dash
        binding.sixthNumberTextView.text = dash
    }

    private fun showError(showing: Boolean, errorMessage: String? = null) {
        val errorTextView = binding.errorTextView
        when (showing) {
            true -> {
                errorTextView.text = errorMessage
                errorTextView.alpha = 1f
                //shake()
            }

            false -> {
                errorTextView.alpha = 0f
                //animate alpha
            }
        }
    }

    private fun verify(code: String) {
        Toast.makeText(this, "Verify $code", Toast.LENGTH_SHORT).show()
    }
}