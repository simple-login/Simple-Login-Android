package io.simplelogin.android.module.startup

import android.content.Intent
import android.os.Bundle
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.login.LoginActivity
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity

class StartupActivity : BaseAppCompatActivity()  {
    private lateinit var binding: ActivityStartUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        val apiKey = SLSharedPreferences.getApiKey(this)
        val intent = if (apiKey != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    override fun onBackPressed() = Unit
}