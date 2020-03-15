package io.simplelogin.android.module.startup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import io.simplelogin.android.R
import io.simplelogin.android.databinding.ActivityStartUpBinding
import io.simplelogin.android.module.home.HomeActivity
import io.simplelogin.android.module.login.LoginActivity
import io.simplelogin.android.utils.SLApiService
import io.simplelogin.android.utils.SLSharedPreferences
import io.simplelogin.android.utils.baseclass.BaseAppCompatActivity
import io.simplelogin.android.utils.enums.SLError
import io.simplelogin.android.utils.extension.toastError
import io.simplelogin.android.utils.model.UserInfo

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
        if (apiKey != null) {
            fetchUserInfoAndProceed(apiKey)
        } else {
            startLoginActivity()
        }
    }

    override fun onBackPressed() = Unit

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun startHomeActivity(userInfo: UserInfo) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(HomeActivity.USER_INFO, userInfo)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still)
    }

    private fun fetchUserInfoAndProceed(apiKey: String) {
        SLApiService.fetchUserInfo(apiKey) { userInfo, error ->
            if (error != null) {
                toastError(error)
                if (error == SLError.InvalidApiKey) {
                    startLoginActivity()
                }

            } else if (userInfo != null) {
                startHomeActivity(userInfo)
            }
        }
    }
}