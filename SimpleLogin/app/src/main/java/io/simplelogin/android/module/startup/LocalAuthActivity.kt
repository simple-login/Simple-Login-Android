package io.simplelogin.android.module.startup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class LocalAuthActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locallyAuthenticate()
    }

    private fun locallyAuthenticate() {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                resetSettingsAndRestartApp()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                resetSettingsAndRestartApp()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle("Local Authentication")
            setSubtitle("Please authenticate to access SimpleLogin")
            setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }.build()

        val executor = ContextCompat.getMainExecutor(this)
        BiometricPrompt(this, executor, callback).authenticate(promptInfo)
    }

    private fun resetSettingsAndRestartApp(){
        SLSharedPreferences.reset(this)
        val intent = Intent(this, StartupActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        Runtime.getRuntime().exit(0)
    }
}