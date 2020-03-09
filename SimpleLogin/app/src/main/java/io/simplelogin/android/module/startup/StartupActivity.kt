package io.simplelogin.android.module.startup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.login.LoginActivity
import io.simplelogin.android.module.login.VerificationActivity
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.toastApiKeyIsNull
import io.simplelogin.android.utils.model.UserLogin

class StartupActivity : BaseAppCompatActivity()  {
    companion object {
        private const val RC_LOGIN = 0
        private const val RC_VERIFICATION = 1
    }

    private lateinit var binding: ActivityStartUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiKey = SLSharedPreferences.getApiKey(this)
        if (apiKey == null) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivityForResult(loginIntent, RC_LOGIN)
        } else {
            startHomeActivity()
        }
    }

    override fun onBackPressed() = Unit

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_LOGIN -> {
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        val userLogin = data?.getParcelableExtra<UserLogin>(LoginActivity.USER_LOGIN)
                        userLogin?.let {
                            when (userLogin.mfaEnabled) {
                                true -> {
                                    if (userLogin.mfaKey != null) {
                                        startVerificationActivity(userLogin.mfaKey)
                                    } else {
                                        Toast.makeText(this, "MFA key is null", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                false -> {
                                    if (userLogin.apiKey!= null) {
                                        SLSharedPreferences.setApiKey(this, userLogin.apiKey)
                                    }
                                    startHomeActivity()
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }

            RC_VERIFICATION -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val apiKey = data?.getStringExtra(VerificationActivity.API_KEY)
                        if (apiKey != null) {
                            SLSharedPreferences.setApiKey(this, apiKey)
                        }
                        startHomeActivity()
                    }

                    else -> Unit
                }
            }

            else -> Unit
        }
    }

    private fun startHomeActivity() {
        val apiKey = SLSharedPreferences.getApiKey(this)

        if (apiKey != null) {
            val homeIntent = Intent(this, HomeActivity::class.java)
            startActivity(homeIntent)
        } else {
            toastApiKeyIsNull()
        }
    }

    private fun startVerificationActivity(mfaKey: String) {
        val verificationIntent = Intent(this, VerificationActivity::class.java)
        verificationIntent.putExtra(VerificationActivity.MFA_KEY, mfaKey)
        startActivityForResult(verificationIntent, RC_VERIFICATION)
    }
}