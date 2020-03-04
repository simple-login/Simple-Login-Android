package io.simplelogin.android.module.startup

import android.content.Intent
import android.os.Bundle
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.login.LoginActivity
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class StartupActivity : BaseAppCompatActivity()  {

    companion object {
        lateinit var binding: ActivityStartUpBinding
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiKey = SLSharedPreferences.getApiKey(this)
        val intent = if (apiKey == null) {
            Intent(this, LoginActivity::class.java)
        } else {
            Intent(this, HomeActivity::class.java)
        }
        startActivity(intent)
    }
}