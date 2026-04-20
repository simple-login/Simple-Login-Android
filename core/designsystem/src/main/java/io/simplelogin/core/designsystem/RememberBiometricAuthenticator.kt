package io.simplelogin.core.designsystem

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun rememberBiometricAuthenticator(
    title: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onError: (String) -> Unit,
    onNotAvailable: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current as? FragmentActivity

    return remember(activity, onSuccess, onError) {
        {
            activity?.let { fragmentActivity ->
                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                val biometricManager = BiometricManager.from(context)
                val canAuthenticate = biometricManager.canAuthenticate(authenticators)

                if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                    onNotAvailable()
                    return@let
                }

                val executor = ContextCompat.getMainExecutor(context)
                val biometricPrompt = BiometricPrompt(
                    fragmentActivity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onSuccess()
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                            ) {
                                onCancel()
                            } else {
                                onError(errString.toString())
                            }
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setAllowedAuthenticators(authenticators)
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}