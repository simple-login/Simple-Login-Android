package io.simplelogin.android.module.login

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
        private const val RC_VERIFICATION = 1
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var facebookCallbackManager: CallbackManager

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

        // Social login
        binding.facebookButton.setOnClickListener { loginWithFacebook() }
        binding.googleButton.setOnClickListener { loginWithGoogle() }
    }

    override fun onBackPressed() = Unit

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_GOOGLE_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }

            RC_VERIFICATION -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val apiKey = data?.getStringExtra(VerificationActivity.API_KEY)
                        apiKey?.let { finalizeLogin(it) }
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

            if (account?.serverAuthCode != null) {
                socialLogin(SocialService.GOOGLE, account.serverAuthCode!!)
            } else {
                Toast.makeText(this, "Google access token is null", Toast.LENGTH_SHORT).show()
            }

        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
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
        when (loading) {
            true -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.rootLinearLayout.alpha = 0.3f
            }

            false -> {
                binding.progressBar.visibility = View.GONE
                binding.rootLinearLayout.alpha = 1f
            }
        }
    }

    private fun processUserLogin(userLogin: UserLogin) {
        when (userLogin.mfaEnabled) {
            true -> userLogin.mfaKey?.let { mfaKey ->
                val verificationIntent = Intent(this, VerificationActivity::class.java)
                verificationIntent.putExtra(
                    VerificationActivity.MFA_MODE,
                    VerificationMode.Mfa(mfaKey)
                )
                startActivityForResult(verificationIntent, RC_VERIFICATION)
            }

            false -> userLogin.apiKey?.let { finalizeLogin(it) }
        }
    }

    private fun finalizeLogin(apiKey: String) {
        SLSharedPreferences.setApiKey(this, apiKey)
        finish()
    }
}