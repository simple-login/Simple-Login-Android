package io.simplelogin.android.module.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityLoginBinding
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.SocialService
import io.simplelogin.android.utils.enums.VerificationMode
import io.simplelogin.android.utils.extension.*
import io.simplelogin.android.utils.model.UserLogin

class LoginActivity : BaseAppCompatActivity() {
    companion object {
        private const val RC_GOOGLE_SIGN_IN = 0 // Request code for Google Sign In
        private const val RC_MFA_VERIFICATION = 1
        private const val RC_EMAIL_VERIFICATION = 2
        private const val RC_SIGN_UP = 3
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var facebookCallbackManager: CallbackManager

    // Forgot password
    private lateinit var forgotPasswordBottomSheetBehavior: BottomSheetBehavior<View>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Change status bar background color
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorWhite)

        // Login
        binding.emailTextField.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
        })

        binding.passwordTextField.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
        })

        binding.loginButton.isEnabled = false // disable login button by default
        binding.loginButton.setOnClickListener { login() }

        // Sign up
        binding.signUpButton.setOnClickListener {
            val signUpIntent = Intent(this, SignUpActivity::class.java)
            startActivityForResult(signUpIntent, RC_SIGN_UP)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
        }

        // Social login
        binding.facebookButton.setOnClickListener { loginWithFacebook() }
        binding.googleButton.setOnClickListener { loginWithGoogle() }

        // Forgot password
        binding.forgotPasswordButton.setOnClickListener { forgotPasswordBottomSheetBehavior.expand() }
        setUpForgotPasswordBottomSheet()

        // App version & About us
        binding.appVersionTextView.text = "SimpleLogin v${getVersionName()}"
        binding.aboutUsTextView.setOnClickListener {
            val aboutActivityIntent = Intent(this, AboutActivity::class.java)
            startActivity(aboutActivityIntent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
        }
    }

    override fun onBackPressed() = Unit

    private fun setUpForgotPasswordBottomSheet() {
        binding.forgotPasswordBottomSheet.root.layoutParams.height = getScreenMetrics().heightPixels

        forgotPasswordBottomSheetBehavior =
            BottomSheetBehavior.from(binding.forgotPasswordBottomSheet.root)
        forgotPasswordBottomSheetBehavior.hide()
        binding.forgotPasswordBottomSheet.cancelButton.setOnClickListener { forgotPasswordBottomSheetBehavior.hide() }

        forgotPasswordBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.dimView.alpha = slideOffset * 60 / 100
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_GOOGLE_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }

            RC_MFA_VERIFICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    val apiKey = data?.getStringExtra(VerificationActivity.API_KEY)
                    apiKey?.let { finalizeLogin(it) }
                }
            }

            RC_EMAIL_VERIFICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    val verificationMode =
                        data?.getParcelableExtra<VerificationMode.AccountActivation>(
                            VerificationActivity.ACCOUNT
                        )
                    binding.emailTextField.editText?.setText(verificationMode?.email)
                    binding.passwordTextField.editText?.setText(verificationMode?.password)
                    login()
                }
            }

            RC_SIGN_UP -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val email = data?.getStringExtra(SignUpActivity.EMAIL) ?: ""
                        val password = data?.getStringExtra(SignUpActivity.PASSWORD) ?: ""
                        signUp(email, password)
                    }

                    else -> Unit
                }
            }

            else -> facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loginWithFacebook() {
        facebookCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email"))
        LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {

                    if (result?.accessToken?.token != null) {
                        socialLogin(SocialService.FACEBOOK, result.accessToken?.token!!)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Facebook access token is null",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancel() {
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook login cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException?) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Facebook login failed: ${error.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun loginWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(getString(R.string.google_web_client_id))
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
//            Log.d("google_auth", "idToken: ${account?.idToken}")
//            Log.d("google_auth", "serverAuthCode ${account?.serverAuthCode}")
            if (account?.serverAuthCode != null) {
                socialLogin(SocialService.GOOGLE, account.serverAuthCode!!)
            } else {
                toastShortly("Google access token is null")
            }

        } catch (e: ApiException) {
            toastShortly("Google sign in failed: ${e.localizedMessage}")
        }
    }

    private fun updateLoginButtonState() {
        val email = binding.emailTextField.editText?.text.toString()
        val password = binding.passwordTextField.editText?.text.toString()

        binding.loginButton.isEnabled = (email != "") && (password != "")
    }

    private fun login() {
        dismissKeyboard()

        val email = binding.emailTextField.editText?.text.toString()
        val password = binding.passwordTextField.editText?.text.toString()
        val deviceName = Build.DEVICE

        if (email != "" && password != "") {
            setLoading(true)
            SLApiService.login(email, password, deviceName) { userLogin, error ->
                runOnUiThread {
                    setLoading(false)
                    if (error != null) {
                        toastError(error)
                    } else if (userLogin != null) {
                        processUserLogin(userLogin)
                    }
                }
            }
        } else {
            toastShortly("Please enter both email and password")
        }
    }

    private fun socialLogin(service: SocialService, accessToken: String) {
        val deviceName = Build.DEVICE
        setLoading(true)
        SLApiService.socialLogin(service, accessToken, deviceName) { userLogin, error ->
            runOnUiThread {
                setLoading(false)
                if (error != null) {
                    toastError(error)
                } else if (userLogin != null) {
                    processUserLogin(userLogin)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.rootLinearLayout.customSetEnabled(!loading)
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun processUserLogin(userLogin: UserLogin) {
        when (userLogin.mfaEnabled) {
            true -> userLogin.mfaKey?.let { mfaKey ->
                startVerificationActivity(VerificationMode.Mfa(mfaKey))
            }

            false -> userLogin.apiKey?.let { finalizeLogin(it) }
        }
    }

    private fun finalizeLogin(apiKey: String) {
        SLSharedPreferences.setApiKey(this, apiKey)
        finish()
    }

    private fun signUp(email: String, password: String) {
        setLoading(true)

        SLApiService.signUp(email, password) { error ->
            runOnUiThread {
                setLoading(false)
                if (error != null) {
                    toastError(error)
                } else {
                    startVerificationActivity(VerificationMode.AccountActivation(email, password))
                }
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