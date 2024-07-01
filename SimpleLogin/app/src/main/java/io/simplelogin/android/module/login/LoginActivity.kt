package io.simplelogin.android.module.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityLoginBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.utils.LoginWithProtonUtils
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.*
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.UserInfo
import io.simplelogin.android.utils.model.UserLogin


class LoginActivity : BaseAppCompatActivity() {
    companion object {
        private const val RC_MFA_VERIFICATION = 0
        private const val RC_EMAIL_VERIFICATION = 1
        private const val RC_SIGN_UP = 2
        private const val BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT = 90.0f / 100
        private const val DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET = 60.0f / 100
    }

    private lateinit var binding: ActivityLoginBinding

    // Forgot password
    private lateinit var forgotPasswordBottomSheetBehavior: BottomSheetBehavior<View>

    // API key
    private lateinit var apiKeyBottomSheetBehavior: BottomSheetBehavior<View>

    // Change API URL
    private lateinit var changeApiUrlBottomSheetBehavior: BottomSheetBehavior<View>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SLSharedPreferences.reset(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Login
        binding.emailTextField.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                updateLoginButtonState()
            }
        })

        binding.emailTextField.editText?.onDrawableEndTouch {
            binding.emailTextField.editText?.text = null
        }

        binding.passwordTextField.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                updateLoginButtonState()
            }
        })

        /*binding.passwordTextField.editText?.onDrawableEndTouch {
            if (binding.passwordTextField.editText?.text.isNullOrEmpty()) return@onDrawableEndTouch
            isShowingPassword = !isShowingPassword
            binding.passwordTextField.editText?.setShowPassword(isShowingPassword)
        }*/

        binding.passwordTextField.editText?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                login()
                return@setOnKeyListener true
            }
            false
        }

        binding.loginButton.isEnabled = false // disable login button by default
        binding.loginButton.setOnClickListener { login() }

        binding.loginWithProtonButton.setOnClickListener {
            loginWithProton()
        }

        // Sign up
        binding.signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivityForResult(signUpIntent, RC_SIGN_UP)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
        }

        // Forgot password
        binding.forgotPasswordButton.setOnClickListener { forgotPasswordBottomSheetBehavior.expand() }
        setUpForgotPasswordBottomSheet()

        // API key
        binding.apiKeyButton.setOnClickListener { apiKeyBottomSheetBehavior.expand() }
        setUpApiKeyBottomSheet()

        // Change API URL
        binding.changeApiUrlButton.setOnClickListener { changeApiUrlBottomSheetBehavior.expand() }
        setUpChangeApiUrlBottomSheet()

        // App version & About us
        binding.appVersionTextView.text = "SimpleLogin v${getVersionName()}"
        binding.aboutUsTextView.setOnClickListener {
            val aboutActivityIntent = Intent(this, AboutActivity::class.java)
            startActivity(aboutActivityIntent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
        }

        binding.root.setOnClickListener { dismissKeyboard() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        forgotPasswordBottomSheetBehavior.hide()
        apiKeyBottomSheetBehavior.hide()
        changeApiUrlBottomSheetBehavior.hide()
        if (forgotPasswordBottomSheetBehavior.isHidden() &&
            apiKeyBottomSheetBehavior.isHidden() &&
            changeApiUrlBottomSheetBehavior.isHidden()
        ) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    /**
     * Callback for when the Login with Proton process is done.
     * The Login with Proton will redirect the user to
     * auth.simplelogin://callback?apikey=YOUR_API_KEY
     *
     * (The intent-filter is registered on the AndroidManifest.xml)
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val apiKey = intent?.data?.getQueryParameter("apikey")
        apiKey?.let { onApiKey(it) }
    }

    private fun setUpForgotPasswordBottomSheet() {
        binding.forgotPasswordBottomSheet.root.layoutParams.height =
            (getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()

        forgotPasswordBottomSheetBehavior =
            BottomSheetBehavior.from(binding.forgotPasswordBottomSheet.root)
        forgotPasswordBottomSheetBehavior.hide()
        binding.forgotPasswordBottomSheet.cancelButton.setOnClickListener { forgotPasswordBottomSheetBehavior.hide() }

        forgotPasswordBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.dimView.visibility = View.GONE
                        dismissKeyboard()
                    }

                    else -> {
                        binding.forgotPasswordBottomSheet.emailTextField.editText?.text = null
                        binding.forgotPasswordBottomSheet.emailTextField.error = null
                        binding.forgotPasswordBottomSheet.emailTextField.requestFocus()
                        showKeyboard()
                        binding.dimView.visibility = View.VISIBLE
                        binding.dimView.setOnTouchListener { _, _ ->
                            // Must return true here to intercept touch event
                            // if not the event is passed to next listener which cause the whole root is clickable
                            true
                        }
                    }
                }
            }
        })

        binding.forgotPasswordBottomSheet.emailTextField.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString().isValidEmail()) {
                    binding.forgotPasswordBottomSheet.resetButton.isEnabled = true
                    binding.forgotPasswordBottomSheet.emailTextField.error = null
                } else {
                    binding.forgotPasswordBottomSheet.resetButton.isEnabled = false
                    binding.forgotPasswordBottomSheet.emailTextField.error = "Invalid email address"
                }
            }
        })

        binding.forgotPasswordBottomSheet.resetButton.setOnClickListener {
            val email = binding.forgotPasswordBottomSheet.emailTextField.editText?.text.toString()
            if (!email.isValidEmail()) return@setOnClickListener

            forgotPasswordBottomSheetBehavior.hide()
            dismissKeyboard()
            setLoading(true)
            SLApiService.forgotPassword(email) {
                runOnUiThread {
                    setLoading(false)
                    toastLongly("We've sent reset password email to \"$email\"")
                }
            }
        }
    }

    private fun setUpApiKeyBottomSheet() {
        binding.apiKeyBottomSheet.root.layoutParams.height =
            (getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()

        apiKeyBottomSheetBehavior = BottomSheetBehavior.from(binding.apiKeyBottomSheet.root)
        apiKeyBottomSheetBehavior.hide()
        binding.apiKeyBottomSheet.cancelButton.setOnClickListener {
            apiKeyBottomSheetBehavior.hide()
        }
        apiKeyBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.dimView.visibility = View.GONE
                        dismissKeyboard()
                    }

                    else -> {
                        binding.apiKeyBottomSheet.apiKeyEditText.text = null
                        binding.apiKeyBottomSheet.apiKeyEditText.requestFocus()
                        showKeyboard()
                        binding.dimView.visibility = View.VISIBLE
                        binding.dimView.setOnTouchListener { _, _ ->
                            // Must return true here to intercept touch event
                            // if not the event is passed to next listener which cause the whole root is clickable
                            true
                        }
                    }
                }
            }
        })

        binding.apiKeyBottomSheet.setButton.setOnClickListener {
            val enteredApiKey = binding.apiKeyBottomSheet.apiKeyEditText.text.toString()
            setLoading(true)
            apiKeyBottomSheetBehavior.hide()
            SLApiService.fetchUserInfo(enteredApiKey) { result ->
                runOnUiThread {
                    setLoading(false)
                    SLSharedPreferences.setApiKey(this, enteredApiKey)
                    result.onSuccess { finalizeLogin(it) }
                    result.onFailure(::toastThrowable)
                }
            }
        }
    }

    private fun setUpChangeApiUrlBottomSheet() {
        binding.changeApiUrlBottomSheet.root.layoutParams.height =
            (getScreenHeight() * BOTTOM_SHEET_HEIGHT_PERCENTAGE_TO_SCREEN_HEIGHT).toInt()

        changeApiUrlBottomSheetBehavior =
            BottomSheetBehavior.from(binding.changeApiUrlBottomSheet.root)
        changeApiUrlBottomSheetBehavior.hide()
        binding.changeApiUrlBottomSheet.cancelButton.setOnClickListener {
            changeApiUrlBottomSheetBehavior.hide()
        }
        changeApiUrlBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * DIM_VIEW_ALPHA_PERCENTAGE_TO_SLIDE_OFFSET
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.dimView.visibility = View.GONE
                        dismissKeyboard()
                    }

                    else -> {
                        val apiUrl = SLSharedPreferences.getApiUrl(this@LoginActivity)
                        binding.changeApiUrlBottomSheet.apiUrlTextField.editText?.setText(apiUrl)
                        binding.changeApiUrlBottomSheet.apiUrlTextField.editText?.placeCursorToEnd()
                        binding.changeApiUrlBottomSheet.apiUrlTextField.editText?.requestFocus()
                        showKeyboard()
                        binding.dimView.visibility = View.VISIBLE
                        binding.dimView.setOnTouchListener { _, _ ->
                            // Must return true here to intercept touch event
                            // if not the event is passed to next listener which cause the whole root is clickable
                            true
                        }
                    }
                }
            }
        })

        binding.changeApiUrlBottomSheet.setButton.setOnClickListener {
            val enteredApiUrl =
                binding.changeApiUrlBottomSheet.apiUrlTextField.editText?.text.toString()
            SLSharedPreferences.setApiUrl(this, enteredApiUrl)
            changeApiUrlBottomSheetBehavior.hide()
            toastShortly("Changed API URL to: $enteredApiUrl")
            SLApiService.setUpBaseUrl(this)
        }

        binding.changeApiUrlBottomSheet.resetButton.setOnClickListener {
            SLSharedPreferences.resetApiUrl(this)
            changeApiUrlBottomSheetBehavior.hide()
            toastShortly("Reset API URL to: ${SLSharedPreferences.getApiUrl(this)}")
            SLApiService.setUpBaseUrl(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_MFA_VERIFICATION ->
                if (resultCode == Activity.RESULT_OK) {
                    val apiKey = data?.getStringExtra(VerificationActivity.API_KEY)
                    apiKey?.let { onApiKey(it) }
                }

            RC_EMAIL_VERIFICATION ->
                if (resultCode == Activity.RESULT_OK) {
                    val verificationMode =
                        data?.getParcelableExtra<VerificationMode.AccountActivation>(
                            VerificationActivity.ACCOUNT
                        )
                    binding.emailTextField.editText?.setText(verificationMode?.email?.value)
                    binding.passwordTextField.editText?.setText(verificationMode?.password?.value)
                    login()
                }

            RC_SIGN_UP ->
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val email = data?.getStringExtra(SignUpActivity.EMAIL) ?: ""
                        val password = data?.getStringExtra(SignUpActivity.PASSWORD) ?: ""
                        signUp(email, password)
                    }

                    else -> Unit
                }

            else -> Unit
        }
    }

    private fun updateLoginButtonState() {
        val email = binding.emailTextField.editText?.text.toString()
        val password = binding.passwordTextField.editText?.text.toString()

        binding.loginButton.isEnabled = email != "" && password != ""
    }

    private fun login() {
        dismissKeyboard()

        val email = binding.emailTextField.editText?.text.toString().trim()
        val password = binding.passwordTextField.editText?.text.toString()
        val deviceName = Build.DEVICE

        if (email.isNotEmpty() && password.isNotEmpty()) {
            setLoading(true)
            SLApiService.login(email, password, deviceName) { result ->
                runOnUiThread {
                    setLoading(false)
                    result.onSuccess(::processUserLogin)
                    result.onFailure {
                        if (it is SLError && it.description == SLError.ResponseError(403).description) {
                            MaterialAlertDialogBuilder(this)
                                .setTitle("WebAuthn currently not supported")
                                .setMessage("Please log in using API key while we are working on supporting WebAuthn on mobile.")
                                .setNeutralButton("Cancel", null)
                                .setPositiveButton("Enter API key") { _, _ ->
                                    apiKeyBottomSheetBehavior.expand()
                                }
                                .show()
                        } else {
                            toastThrowable(it)
                        }
                    }
                }
            }
        } else {
            toastShortly("Please enter both email and password")
        }
    }

    private fun loginWithProton() {
        dismissKeyboard()
        LoginWithProtonUtils.launchLoginWithProton(this)
    }

    private fun setLoading(loading: Boolean) {
        binding.rootLinearLayout.customSetEnabled(!loading)
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun processUserLogin(userLogin: UserLogin) {
        when (userLogin.mfaEnabled) {
            true -> userLogin.mfaKey?.let {
                startVerificationActivity(VerificationMode.Mfa(MfaKey(it)))
            }

            false -> userLogin.apiKey?.let { onApiKey(userLogin.apiKey) }
        }
    }

    private fun onApiKey(apiKey: String) {
        SLSharedPreferences.setApiKey(this, apiKey)
        SLApiService.fetchUserInfo(apiKey) { result ->
            result.onSuccess(::finalizeLogin)
            result.onFailure(::toastThrowable)
        }
    }

    private fun finalizeLogin(userInfo: UserInfo) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.USER_INFO, userInfo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
        finish()
    }

    private fun signUp(email: String, password: String) {
        setLoading(true)

        SLApiService.signUp(email, password) { result ->
            runOnUiThread {
                setLoading(false)

                result.onSuccess {
                    toastLongly("Check your inbox for verification code")
                    val mode = VerificationMode.AccountActivation(Email(email), Password(password))
                    startVerificationActivity(mode)
                }

                result.onFailure(::toastThrowable)
            }
        }
    }

    private fun startVerificationActivity(verificationMode: VerificationMode) {
        val verificationIntent = Intent(this, VerificationActivity::class.java)

        when (verificationMode) {
            is VerificationMode.Mfa -> {
                verificationIntent.putExtra(
                    VerificationActivity.MFA_MODE,
                    verificationMode
                )

                startActivityForResult(verificationIntent, RC_MFA_VERIFICATION)
            }

            is VerificationMode.AccountActivation -> {
                verificationIntent.putExtra(
                    VerificationActivity.ACCOUNT_ACTIVATION_MODE,
                    verificationMode
                )

                startActivityForResult(verificationIntent, RC_EMAIL_VERIFICATION)
            }
        }

        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }
}
