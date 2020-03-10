package io.simplelogin.android.module.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivitySignUpBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

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

        binding.signUpButton.setOnClickListener { finishWithEmailAndPassword() }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down)
        }
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