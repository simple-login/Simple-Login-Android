package io.simplelogin.android.module.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityLoginBinding
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.SocialService

class LoginActivity : BaseAppCompatActivity() {
    companion object {
        lateinit var binding: ActivityLoginBinding
            private set
        private const val RC_SIGN_IN = 0 // Request code for Google Sign In
    }

    private lateinit var facebookCallbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.facebookButton.setOnClickListener { loginWithFacebook() }
        binding.googleButton.setOnClickListener { loginWithGoogle() }
    }

    private fun loginWithFacebook() {
        facebookCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email"))
        LoginManager.getInstance().registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {

                if (result?.accessToken?.token != null) {
                    socialLogin(SocialService.FACEBOOK, result.accessToken?.token!!)
                } else {
                    Toast.makeText(this@LoginActivity, "Facebook access token is null", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@LoginActivity, "Facebook login failed: ${error.toString()}", Toast.LENGTH_SHORT).show()
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
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }
            else -> facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        }
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
            Toast.makeText(this, "Google sign in failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun socialLogin(service: SocialService, accessToken: String) {
        val deviceName = Build.DEVICE

        SLApiService.socialLogin(service, accessToken, deviceName)
    }
}