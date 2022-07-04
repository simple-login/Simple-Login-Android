package io.simplelogin.android.module.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityVerificationBinding
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.Email
import io.simplelogin.android.utils.enums.MfaKey
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.enums.VerificationMode
import io.simplelogin.android.utils.extension.*

class VerificationActivity : BaseAppCompatActivity(), Window.Callback {
    companion object {
        const val MFA_MODE = "mfaMode"
        const val ACCOUNT_ACTIVATION_MODE = "accountActivationMode"
        const val API_KEY = "apiKey"
        const val ACCOUNT = "account"
    }

    private val DELAY_AFTER_RESET_MS = 100L

    private lateinit var binding: ActivityVerificationBinding
    private lateinit var verificationMode: VerificationMode
    private val dash = "-"

    @SuppressLint("SetTextI18n")
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
            is VerificationMode.Mfa ->
                binding.toolbarTitleText.text = "Enter OTP"

            is VerificationMode.AccountActivation ->
                binding.toolbarTitleText.text = "Enter activation code"
        }

        reset()
        setUpClickListeners()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        getCodeFromClipboard()?.let { code ->
            Snackbar.make(binding.root, "\"$code\" is found in the clipboard", Snackbar.LENGTH_INDEFINITE)
                .setAction("Paste & Verify") {
                    pasteAndVerify(code)
                }
                .show()
        }
    }

    private fun getVerificationMode(): VerificationMode {
        val mfa = intent.getParcelableExtra<VerificationMode.Mfa>(MFA_MODE)

        if (mfa != null) return mfa

        val accountActivation = intent.getParcelableExtra<VerificationMode.AccountActivation>(
            ACCOUNT_ACTIVATION_MODE
        )

        if (accountActivation != null) return accountActivation

        throw IllegalStateException("VerificationMode not found in intent")
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

    @Suppress("MagicNumber", "ReturnCount")
    private fun getCodeFromClipboard() : String? {
        val clipboardManager =
            (getSystemService(Context.CLIPBOARD_SERVICE) ?: return null) as ClipboardManager
        val clip = clipboardManager.primaryClip ?: return null
        val item = clip.getItemAt(0) ?: return null
        val copiedCharacterSequence = item.text ?: return null

        if (copiedCharacterSequence.count() != 6) return null

        val copiedString = copiedCharacterSequence.toString()
        copiedString.toIntOrNull() ?: return null

        return copiedString
    }

    private fun pasteAndVerify(code: String) {
        reset()
        Handler(Looper.getMainLooper()).postDelayed({
            code.asIterable().forEach { char ->
                addNumber(char.toString(), false)
            }

            verify(code)
        }, DELAY_AFTER_RESET_MS)
    }

    private fun addNumber(number: String, launchVerify: Boolean = true) {
        when {
            binding.firstNumberTextView.text == dash -> {
                binding.firstNumberTextView.text = number
                showError(false)
            }
            binding.secondNumberTextView.text == dash -> binding.secondNumberTextView.text = number
            binding.thirdNumberTextView.text == dash -> binding.thirdNumberTextView.text = number
            binding.fourthNumberTextView.text == dash -> binding.fourthNumberTextView.text = number
            binding.fifthNumberTextView.text == dash -> binding.fifthNumberTextView.text = number
            binding.sixthNumberTextView.text == dash -> {
                binding.sixthNumberTextView.text = number
                var code = ""
                code += binding.firstNumberTextView.text
                code += binding.secondNumberTextView.text
                code += binding.thirdNumberTextView.text
                code += binding.fourthNumberTextView.text
                code += binding.fifthNumberTextView.text
                code += binding.sixthNumberTextView.text

                if (launchVerify) {
                    verify(code)
                }
            }
        }
    }

    private fun deleteLastNumber() {
        when {
            binding.sixthNumberTextView.text != dash -> binding.sixthNumberTextView.text = dash
            binding.fifthNumberTextView.text != dash -> binding.fifthNumberTextView.text = dash
            binding.fourthNumberTextView.text != dash -> binding.fourthNumberTextView.text = dash
            binding.thirdNumberTextView.text != dash -> binding.thirdNumberTextView.text = dash
            binding.secondNumberTextView.text != dash -> binding.secondNumberTextView.text = dash
            binding.firstNumberTextView.text != dash -> binding.firstNumberTextView.text = dash
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
                errorTextView.shake()
            }
            false -> errorTextView.fadeOut()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootLinearLayout.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun verify(code: String) {
        when (verificationMode) {
            is VerificationMode.Mfa -> {
                val mfaKey = (verificationMode as VerificationMode.Mfa).mfaKey
                verify(mfaKey, code)
            }

            is VerificationMode.AccountActivation -> {
                val email = (verificationMode as VerificationMode.AccountActivation).email
                verify(email, code)
            }
        }
    }

    private fun verify(mfaKey: MfaKey, code: String) {
        setLoading(true)
        SLApiService.verifyMfa(mfaKey, code, Build.DEVICE) { result ->
            runOnUiThread {
                setLoading(false)
                result.onSuccess { apiKey ->
                    val returnIntent = Intent()
                    returnIntent.putExtra(API_KEY, apiKey.value)
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }

                result.onFailure { error ->
                    showError(true, error.localizedMessage)
                    reset()
                }
            }
        }
    }

    private fun verify(email: Email, code: String) {
        setLoading(true)
        SLApiService.verifyEmail(email, code) { result ->
            runOnUiThread {
                setLoading(false)
                result.onSuccess {
                    val returnIntent = Intent()
                    returnIntent.putExtra(
                        ACCOUNT,
                        verificationMode as VerificationMode.AccountActivation
                    )
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }

                result.onFailure { error ->
                    when (error) {
                        is SLError.ReactivationNeeded -> showReactivationAlert(email)

                        else -> {
                            showError(true, error.localizedMessage)
                            reset()
                        }
                    }
                }
            }
        }
    }

    private fun showReactivationAlert(email: Email) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Wrong code too many times")
            .setMessage("We will send you a new activation code to \"${email.value}\"")
            .setPositiveButton("Close", null)
            .setOnDismissListener {
                requestNewCode(email)
                reset()
            }
            .show()
    }

    private fun requestNewCode(email: Email) {
        setLoading(true)
        SLApiService.reactivate(email) { result ->
            runOnUiThread {
                setLoading(false)
                result.onSuccess { toastShortly("Check your inbox for new activation code") }
                result.onFailure(::toastThrowable)
            }
        }
    }
}
