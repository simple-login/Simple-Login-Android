package io.simplelogin.android.module.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivitySignUpBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.extension.dismissKeyboard
import io.simplelogin.android.utils.extension.isValidEmail

class SignUpActivity : BaseAppCompatActivity() {
    companion object {
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }
    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cancelButton.setOnClickListener { finish() }

        binding.signUpButton.isEnabled = false
        binding.signUpButton.setOnClickListener { finishWithEmailAndPassword() }

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                verifyEnteredEmailAndPassword()
            }
        }

        binding.emailTextField.editText?.addTextChangedListener(textWatcher)
        binding.passwordTextField.editText?.addTextChangedListener(textWatcher)

        binding.root.setOnClickListener { dismissKeyboard() }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
    }

    @Suppress("MagicNumber")
    private fun verifyEnteredEmailAndPassword() {
        // Verify email
        val email = binding.emailTextField.editText?.text.toString()
        val isValidEmail = email.isValidEmail()

        if (isValidEmail) {
            binding.emailTextField.error = null
        } else {
            binding.emailTextField.error = "Invalid email"
        }

        // Verify password
        val password = binding.passwordTextField.editText?.text.toString()
        val isValidPassword = when (password.count()) {
            in 0..7 -> false
            else -> true
        }

        if (isValidPassword) {
            binding.passwordTextField.error = null
        } else {
            binding.passwordTextField.error = "Minimum 8 characters is required"
        }

        binding.signUpButton.isEnabled = isValidEmail && isValidPassword
    }

    private fun finishWithEmailAndPassword() {
        val email = binding.emailTextField.editText?.text.toString()
        val password = binding.passwordTextField.editText?.text.toString()

        val returnIntent = Intent()
        returnIntent.putExtra(EMAIL, email)
        returnIntent.putExtra(PASSWORD, password)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
