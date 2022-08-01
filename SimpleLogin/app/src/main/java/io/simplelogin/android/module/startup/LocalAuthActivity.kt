package io.simplelogin.android.module.startup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.simplelogin.android.databinding.ActivityLocalAuthBinding
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class LocalAuthActivity : BaseAppCompatActivity() {
    private lateinit var binding: ActivityLocalAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locallyAuthenticate()
    }

    private fun locallyAuthenticate() {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                authenticateAgainPrompt()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authenticateAgainPrompt()
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

    private fun authenticateAgainPrompt() {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle("Failed to authenticate")
            .setPositiveButton("Try again") { _, _ -> locallyAuthenticate() }
            .setNegativeButton("Sign out") { _, _ -> resetSettingsAndRestartApp() }
            .show()
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