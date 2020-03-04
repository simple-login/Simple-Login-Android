package io.simplelogin.android.module.login

import android.os.Bundle
import io.simplelogin.android.databinding.ActivityLoginBinding
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class LoginActivity : BaseAppCompatActivity() {
    companion object {
        lateinit var binding: ActivityLoginBinding
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}