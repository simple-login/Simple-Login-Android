package io.simplelogin.android.module.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import io.simplelogin.android.databinding.ActivityLoginBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class LoginActivity : BaseAppCompatActivity() {
    companion object {
        lateinit var binding: ActivityLoginBinding
            private set
    }

    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.facebookButton.setOnClickListener { loginWithFacebook() }
    }

    // Facebook login
    private fun loginWithFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                result?.let {
                    Log.d("fb", it.accessToken.token)
                }
            }

            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook login cancelled", Toast.LENGTH_SHORT)
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@LoginActivity, "Facebook login failed: ${error.toString()}", Toast.LENGTH_SHORT)
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}